import Deserializer.CsvDeserializer;
import Models.Person;
import Models.XML.Dataset;
import Models.YAML.YamlResponse;
import ThreadsRunner.Initializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static ThreadsRunner.Initializer.mapData;

public class DataParser {

    public static void convertData() throws JsonProcessingException, IllegalAccessException, NoSuchFieldException, InstantiationException {
        if (mapData != null) {
            {
                for (Map.Entry<String, String> entry : mapData.entrySet()) {
                    String k = entry.getKey();
                    String v = entry.getValue();
                    if (Objects.nonNull(v)) {
                        switch (v) {
                            case "application/xml": {
                                XmlMapper xmlMapper = new XmlMapper();
                                Initializer.personList.addAll(xmlMapper.readValue(k, Dataset.class).getRecord());
                                break;
                            }
                            case "text/csv": {
                                Initializer.personList.addAll((List<Person>) (Object) new ArrayList<>(CsvDeserializer.convertCsv(k, Person.class)));
                                break;
                            }
                            case "application/x-yaml": {
                                YAMLMapper yamlMapper = new YAMLMapper();
                                Initializer.personList.addAll(yamlMapper.readValue(k, new TypeReference<ArrayList<YamlResponse>>() {
                                }));
                                break;
                            }
                            case "application/json": {
                                ObjectMapper jsonMapper = new ObjectMapper();
                                if (k.substring(k.length() - 3).contains(",")) {
                                    k = k.substring(0, k.lastIndexOf(",")) + "]";
                                }
                                Initializer.personList.addAll(jsonMapper.readValue(k, new TypeReference<ArrayList<Person>>() {
                                }));
                                break;
                            }
                        }
                    }
                }
            }
//            for (Person p : personList) {
//                System.out.println(p.toString());
//            }
        }
    }
}
