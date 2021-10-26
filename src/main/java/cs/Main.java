package cs;


import cs.qse.EndpointParser;
import cs.qse.Parser;
import cs.utils.ConfigManager;
import cs.utils.Constants;
import cs.utils.Utils;


public class Main {
    public static String configPath;
    public static String datasetPath;
    public static int numberOfClasses;
    public static int numberOfInstances;
    
    public static void main(String[] args) throws Exception {
        configPath = args[0];
        datasetPath = ConfigManager.getProperty("dataset_path");
        numberOfClasses = Integer.parseInt(ConfigManager.getProperty("expected_number_classes")); // expected or estimated numberOfClasses
        numberOfInstances = Integer.parseInt(ConfigManager.getProperty("expected_number_of_lines")); // expected or estimated numberOfInstances
        benchmark();
    }
    
    private static void benchmark() {
        System.out.println("Benchmark Initiated for " + ConfigManager.getProperty("dataset_path"));
        Utils.getCurrentTimeStamp();
        new Parser(datasetPath, numberOfClasses, numberOfInstances, Constants.RDF_TYPE).run();
        try {
            if (isOn("QSE_File_Schema_Extractor")) {
                System.out.println("Parser");
                new Parser(datasetPath, numberOfClasses, numberOfInstances, Constants.RDF_TYPE).run();
            }
    
            if (isOn("QSE_Endpoint_Schema_Extractor")) {
                System.out.println("EndpointSchemaExtractor - Using SPARQL Queries");
                new EndpointParser().run();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static boolean isOn(String option) {return Boolean.parseBoolean(ConfigManager.getProperty(option));}
}
