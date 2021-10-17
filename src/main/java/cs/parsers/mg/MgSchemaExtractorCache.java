package cs.parsers.mg;

import cs.qse.SHACLER;
import cs.utils.*;
import cs.utils.encoders.NodeEncoder;
import orestes.bloomfilter.BloomFilter;
import orestes.bloomfilter.FilterBuilder;
import org.apache.commons.lang3.time.StopWatch;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.ehcache.sizeof.SizeOf;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.nx.parser.ParseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class MgSchemaExtractorCache {
    String rdfFile;
    SHACLER shacler = new SHACLER();
    
    Integer expectedNumberOfClasses;
    Integer membershipGraphRootNode;
    HashMap<String, Integer> classInstanceCount;
    HashMap<Node, HashMap<Node, HashSet<String>>> classToPropWithObjTypes;
    HashMap<String, HashMap<Node, Integer>> classToPropWithCount;
    
    HashMap<Node, List<Integer>> instanceToClass;
    HashSet<Node> properties;
    NodeEncoder encoder;
    DefaultDirectedGraph<Integer, DefaultEdge> membershipGraph;
    HashMap<Integer, BloomFilter<String>> ctiBf;
    MembershipGraph mg;
    
    public MgSchemaExtractorCache(String filePath, int expSizeOfClasses) {
        this.rdfFile = filePath;
        this.expectedNumberOfClasses = expSizeOfClasses;
        this.classInstanceCount = new HashMap<>((int) ((expectedNumberOfClasses) / 0.75 + 1)); //0.75 is the load factor
        this.classToPropWithObjTypes = new HashMap<>((int) ((expectedNumberOfClasses) / 0.75 + 1));
        this.classToPropWithCount = new HashMap<>((int) ((expectedNumberOfClasses) / 0.75 + 1));
        int nol = Integer.parseInt(ConfigManager.getProperty("expected_number_of_lines"));
        this.instanceToClass = new HashMap<>((int) ((nol) / 0.75 + 1));
        this.properties = new HashSet<>((int) (1000 * 1.33));
        this.encoder = new NodeEncoder();
        this.ctiBf = new HashMap<>((int) ((expectedNumberOfClasses) / 0.75 + 1));
    }
    
    private void preFirstPass() {
        StopWatch watch = new StopWatch();
        watch.start();
        try {
            Files.lines(Path.of(rdfFile))
                    .filter(line -> line.contains(Constants.RDF_TYPE))
                    .forEach(line -> {
                        try {
                            Node[] nodes = NxParser.parseNodes(line);
                            classInstanceCount.put(nodes[2].getLabel(), (classInstanceCount.getOrDefault(nodes[2].getLabel(), 0)) + 1);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
        watch.stop();
        System.out.println("Time Elapsed preFirstPass: " + TimeUnit.MILLISECONDS.toSeconds(watch.getTime()) + " : " + TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
    }
    
    private void firstPass() {
        StopWatch watch = new StopWatch();
        watch.start();
        try {
            Files.lines(Path.of(rdfFile))
                    .filter(line -> line.contains(Constants.RDF_TYPE))
                    .forEach(line -> {
                        try {
                            Node[] nodes = NxParser.parseNodes(line);
                            //classInstanceCount.put(nodes[2].getLabel(), (classInstanceCount.getOrDefault(nodes[2].getLabel(), 0)) + 1);
                            
                            // Track classes per instance
                            if (instanceToClass.containsKey(nodes[0])) {
                                instanceToClass.get(nodes[0]).add(encoder.encode(nodes[2]));
                            } else {
                                List<Integer> list = new ArrayList<>();
                                list.add(encoder.encode(nodes[2]));
                                instanceToClass.put(nodes[0], list);
                            }
                            
                            if (ctiBf.containsKey(encoder.encode(nodes[2]))) {
                                ctiBf.get(encoder.encode(nodes[2])).add(nodes[0].getLabel());
                            } else {
                                BloomFilter<String> bf = new FilterBuilder(classInstanceCount.get(nodes[2].getLabel()), 0.00001).buildBloomFilter();
                                bf.add(nodes[0].getLabel());
                                ctiBf.put(encoder.encode(nodes[2]), bf);
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
        watch.stop();
        System.out.println("Time Elapsed firstPass: " + TimeUnit.MILLISECONDS.toSeconds(watch.getTime()) + " : " + TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
    }
    
    private void membershipGraphConstruction() {
        Utils.getCurrentTimeStamp();
        StopWatch watch = new StopWatch();
        watch.start();
        this.mg = new MembershipGraph(encoder, ctiBf, classInstanceCount);
        mg.createMembershipSets(instanceToClass);
        mg.createMembershipGraph();
        mg.membershipGraphCompression(Integer.parseInt(ConfigManager.getProperty("mg_threshold")));
        //mg.exportGraphRelatedData();
        //mg.importGraphRelatedData();
        this.membershipGraph = mg.getMembershipGraph();
        this.membershipGraphRootNode = mg.getMembershipGraphRootNode();
        this.ctiBf = mg.getCtiBf();
        
        watch.stop();
        System.out.println("Time Elapsed MembershipGraphConstruction: " + TimeUnit.MILLISECONDS.toSeconds(watch.getTime()) + " : " + TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
    }
    
    private void secondPass() {
        System.out.println("2nd Pass Started");
        Utils.getCurrentTimeStamp();
        StopWatch watch = new StopWatch();
        watch.start();
        //LRUCache cache = new LRUCache(1000000);
        
        Cache<Node, List<Node>> cache = new Cache2kBuilder<Node, List<Node>>() {}
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .entryCapacity(1000000)
                .build();
        
        try {
            Files.lines(Path.of(rdfFile))
                    .filter(line -> !line.contains(Constants.RDF_TYPE))
                    .forEach(line -> {
                        try {
                            Node[] nodes = NxParser.parseNodes(line);
                            List<Node> instanceTypes = new ArrayList<>();
                            HashSet<String> objTypes = new HashSet<String>();
                            
                            if (Utils.isValidIRI(nodes[2].getLabel())) {
                                IRI object = Utils.toIri(nodes[2].getLabel());
                                if (object.isLiteral()) {
                                    if (cache.containsKey(nodes[0])) {
                                        instanceTypes.addAll(cache.get(nodes[0]));
                                    } else {
                                        traverseMgForSubject(cache, nodes[0], instanceTypes);
                                    }
                                } else {
                                    //check if cache contains subject's type?
                                    if (cache.containsKey(nodes[0])) {
                                        instanceTypes.addAll(cache.get(nodes[0]));
                                    } else {
                                        traverseMgForSubject(cache, nodes[0], instanceTypes);
                                    }
                                    
                                    //check if cache contains object's type?
                                    if (cache.containsKey(nodes[2])) {
                                        cache.get(nodes[2]).forEach(val -> {objTypes.add(val.getLabel());});
                                    } else {
                                        traverseMgForObject(cache, nodes[2], objTypes);
                                    }
                                }
                            }
                            
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
        System.out.println("Time Elapsed secondPass: " + TimeUnit.MILLISECONDS.toSeconds(watch.getTime()) + " : " + TimeUnit.MILLISECONDS.toMinutes(watch.getTime()));
    }
    
    private void traverseMgForSubject(Cache<Node, List<Node>> cache, Node subjectNode, List<Node> instanceTypes) {
        int node = this.membershipGraphRootNode;
        HashSet<Integer> visited = new HashSet<>(expectedNumberOfClasses);
        LinkedList<Integer> queue = new LinkedList<Integer>();
        queue.add(node);
        visited.add(node);
        while (queue.size() != 0) {
            node = queue.poll();
            for (DefaultEdge edge : membershipGraph.outgoingEdgesOf(node)) {
                Integer neigh = membershipGraph.getEdgeTarget(edge);
                if (!visited.contains(neigh)) {
                    boolean flag = false;
                    if (ctiBf.get(neigh).contains(subjectNode.getLabel())) {
                        instanceTypes.add(encoder.decode(neigh));
                        
                        if (cache.containsKey(subjectNode)) {
                            cache.get(subjectNode).add(encoder.decode(neigh));
                        } else {
                            cache.put(subjectNode, new ArrayList<>() {{
                                add(encoder.decode(neigh));
                            }});
                        }
                        flag = true;
                    }
                    if (flag) {
                        queue.add(neigh);
                    }
                    visited.add(neigh);
                }
            }
        }
    }
    
    private void traverseMgForObject(Cache<Node, List<Node>> cache, Node objectNode, HashSet<String> objTypes) {
        int node = this.membershipGraphRootNode;
        HashSet<Integer> visited = new HashSet<>(expectedNumberOfClasses);
        LinkedList<Integer> queue = new LinkedList<Integer>();
        queue.add(node);
        visited.add(node);
        while (queue.size() != 0) {
            node = queue.poll();
            for (DefaultEdge edge : membershipGraph.outgoingEdgesOf(node)) {
                Integer neigh = membershipGraph.getEdgeTarget(edge);
                if (!visited.contains(neigh)) {
                    boolean flag = false;
                    if (ctiBf.get(neigh).contains(objectNode.getLabel())) {
                        objTypes.add(encoder.decode(neigh).getLabel());
                        
                        if (cache.containsKey(objectNode)) {
                            cache.get(objectNode).add(encoder.decode(neigh));
                        } else {
                            cache.put(objectNode, new ArrayList<>() {{
                                add(encoder.decode(neigh));
                            }});
                        }
                        flag = true;
                    }
                    if (flag) {
                        queue.add(neigh);
                    }
                    visited.add(neigh);
                }
            }
        }
    }
    
    private void populateShapes() {
        Utils.getCurrentTimeStamp();
        System.out.println("Populate Shapes Method Invoked:");
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
    
    private void measureMemoryUsage() {
        SizeOf sizeOf = SizeOf.newInstance();
        System.out.println("Size - Parser HashMap<Integer, BloomFilter<String>> ctiBf: " + sizeOf.deepSizeOf(ctiBf));
        System.out.println("Size - Parser HashMap<String, Integer> classInstanceCount: " + sizeOf.deepSizeOf(classInstanceCount));
        System.out.println("Size - Encoder object encoder: " + sizeOf.deepSizeOf(encoder.getTable()));
        System.out.println("Size - Parser HashMap<Node, List<Node>> instanceToClass: " + sizeOf.deepSizeOf(instanceToClass));
        System.out.println("Size - Parser HashSet<String> properties: " + sizeOf.deepSizeOf(properties));
        System.out.println("Size - Parser HashMap<String, HashMap<String, HashSet<String>>> classToPropWithObjTypes: " + sizeOf.deepSizeOf(classToPropWithObjTypes));
    }
    
    private void runParser() throws IOException {
        preFirstPass();
        firstPass();
        membershipGraphConstruction();
        this.instanceToClass.clear();
        secondPass();
        //populateShapes();
        //shacler.writeModelToFile();
        //System.out.println("OUT DEGREE OF HNG ROOT NODE: " + membershipGraph.outDegreeOf(membershipGraphRootNode));
        System.out.println("STATS: \n\t" + "No. of Classes: " + classInstanceCount.size() + "\n\t" + "No. of distinct Properties: " + properties.size());
    }
    
    
    public void run() throws IOException {
        runParser();
        //measureMemoryUsage();
    }
}