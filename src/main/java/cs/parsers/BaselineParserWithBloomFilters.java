package cs.parsers;

import cs.qse.SHACLER;
import cs.utils.Constants;
import orestes.bloomfilter.BloomFilter;
import orestes.bloomfilter.FilterBuilder;
import org.apache.commons.lang3.time.StopWatch;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.ehcache.sizeof.SizeOf;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.nx.parser.ParseException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class BaselineParserWithBloomFilters {
    String rdfFile;
    SHACLER shacler = new SHACLER();
    Integer expectedNumberOfClasses;
    HashMap<String, Integer> classInstanceCount;
    HashMap<Node, HashMap<Node, HashSet<String>>> classToPropWithObjTypes;
    HashSet<Node> properties;
    HashMap<Node, BloomFilter<String>> ctiBf;
    
    // Constructor
    public BaselineParserWithBloomFilters(String filePath, int expSizeOfClasses) {
        this.rdfFile = filePath;
        this.expectedNumberOfClasses = expSizeOfClasses;
        this.classInstanceCount = new HashMap<>((int) ((expectedNumberOfClasses) / 0.75 + 1)); //0.75 is the load factor
        this.classToPropWithObjTypes = new HashMap<>((int) ((expectedNumberOfClasses) / 0.75 + 1));
        this.properties = new HashSet<>((int) (1000 * 1.33));
        this.ctiBf = new HashMap<>((int) ((expectedNumberOfClasses) / 0.75 + 1));
    }
    
    private void firstPass() {
        StopWatch watch = new StopWatch();
        watch.start();
        try {
            Files.lines(Path.of(rdfFile))                           // - Stream of lines ~ Stream <String>
                    .filter(line -> line.contains(Constants.RDF_TYPE))
                    .forEach(line -> {                              // - A terminal operation
                        try {
                            Node[] nodes = NxParser.parseNodes(line);
                            classInstanceCount.put(nodes[2].toString(), (classInstanceCount.getOrDefault(nodes[2].toString(), 0)) + 1);
                            
                            if (ctiBf.containsKey(nodes[2])) {
                                ctiBf.get(nodes[2]).add(nodes[0].getLabel());
                            } else {
                                BloomFilter<String> bf = new FilterBuilder(100_000, 0.000001).buildBloomFilter();
                                bf.add(nodes[0].getLabel());
                                ctiBf.put(nodes[2], bf);
                            }
                            
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    });
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        watch.stop();
        System.out.println("Time Elapsed firstPass: " + TimeUnit.MILLISECONDS.toSeconds(watch.getTime()));
    }
    
    private void secondPass() {
        StopWatch watch = new StopWatch();
        watch.start();
        try {
            Files.lines(Path.of(rdfFile))                           // - Stream of lines ~ Stream <String>
                    .filter(line -> !line.contains(Constants.RDF_TYPE))        // - Exclude RDF type triples
                    .forEach(line -> {                              // - A terminal operation
                        try {
                            Node[] nodes = NxParser.parseNodes(line);
                            
                            //What's the type of this instance? To find the type, you need to iterate over the ctiBf and check in all the bloom filters
                            List<Node> instanceTypes = new ArrayList<>();
                            HashSet<String> objTypes = new HashSet<String>();
                            ctiBf.forEach((c, bf) -> {
                                if (bf.contains(nodes[0].getLabel())) {
                                    instanceTypes.add(c);
                                }
                                if (bf.contains(nodes[2].getLabel())) {
                                    objTypes.add(c.getLabel());
                                }
                            });
                            
                            instanceTypes.forEach(c -> {
                                if (objTypes.isEmpty()) {
                                    objTypes.add(getType(nodes[2].toString()));
                                }
                                
                                if (classToPropWithObjTypes.containsKey(c)) {
                                    HashMap<Node, HashSet<String>> propToObjTypes = classToPropWithObjTypes.get(c);
                                    
                                    if (propToObjTypes.containsKey(nodes[1]))
                                        propToObjTypes.get(nodes[1]).addAll(objTypes);
                                    else {
                                        propToObjTypes.put(nodes[1], objTypes);
                                    }
                                    classToPropWithObjTypes.put(c, propToObjTypes);
                                    
                                } else {
                                    HashMap<Node, HashSet<String>> propToObjTypes = new HashMap<>();
                                    propToObjTypes.put(nodes[1], objTypes);
                                    instanceTypes.forEach(type -> {
                                        classToPropWithObjTypes.put(type, propToObjTypes);
                                    });
                                }
                            });
                            properties.add(nodes[1]);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    });
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        watch.stop();
        System.out.println("Time Elapsed secondPass: " + TimeUnit.MILLISECONDS.toSeconds(watch.getTime()));
    }
    
    private void populateShapes() {
        StopWatch watch = new StopWatch();
        watch.start();
        classToPropWithObjTypes.forEach((c, p) -> {
            shacler.setParams(c, p);
            shacler.constructShape();
        });
        watch.stop();
        System.out.println("Time Elapsed populateShapes: " + TimeUnit.MILLISECONDS.toSeconds(watch.getTime()));
    }
    
    private String getType(String value) {
        String theType = "<http://www.w3.org/2001/XMLSchema#string>"; //default type is XSD:string
        
        if (value.contains("\"^^")) {
            //value.split("\\^\\^")[0] is the literal value, and value.split("\\^\\^")[1]  the type of the value ;
            if (value.split("\\^\\^").length > 1) {
                theType = value.split("\\^\\^")[1];
            }
        } else if (value.contains("\"@")) {
            //value.split("\"@")[0] is the literal value and value.split("\"@")[1] is the language tag
            theType = RDF.LANGSTRING.toString();  //rdf:langString
        }
        return theType;
    }
    
    private void runParser() {
        firstPass();
        secondPass();
        System.out.println("STATS: \n\t" + "No. of Classes: " + classInstanceCount.size() + "\n\t" + "No. of distinct Properties: " + properties.size());
        populateShapes();
        shacler.writeModelToFile();
    }
    
    private void measureMemoryUsage() {
        SizeOf sizeOf = SizeOf.newInstance();
        System.out.println("Size - Parser HashMap<String, Integer> classInstanceCount: " + sizeOf.deepSizeOf(classInstanceCount));
        System.out.println("Size - Parser HashSet<String> properties: " + sizeOf.deepSizeOf(properties));
        System.out.println("Size - Parser HashMap<Node, BloomFilter<CharSequence>> ctiBf: " + sizeOf.deepSizeOf(ctiBf));
        System.out.println("Size - Parser HashMap<String, HashMap<String, HashSet<String>>> classToPropWithObjTypes: " + sizeOf.deepSizeOf(classToPropWithObjTypes));
    }
    
    public void run() {
        runParser();
        //measureMemoryUsage();
    }
}
