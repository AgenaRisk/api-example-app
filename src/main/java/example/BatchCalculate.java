package example;

import com.agenarisk.api.model.DataSet;
import com.agenarisk.api.model.Model;
import com.agenarisk.api.model.Network;
import com.agenarisk.api.model.Node;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Eugene
 */
public class BatchCalculate {
	public static void main(String[] args) throws Exception {
		String separatorIn = ",";
		String separatorOut = ",";
		List<String> rawData = Files.lines(new File(Demo.class.getClassLoader().getResource("example/Asia.csv").getFile()).toPath()).collect(Collectors.toList());
		String[] headers = rawData.get(0).split(separatorIn);
		List<String[]> data = rawData.subList(1, rawData.size()-1).stream().map(line -> line.split(",")).collect(Collectors.toList());
		
		String modelFilePath = new File(Demo.class.getClassLoader().getResource("example/Asia.xml").getFile()).getAbsolutePath();
		Model model = Model.loadModel(modelFilePath);
		Network net = model.getNetworkList().get(0);

		// Delete pre-exising DataSets
		if (!model.getDataSets().isEmpty()){
			model.removeDataSet(model.getDataSetList().get(0));
		}
		
		// If you want to use a single DataSet then for each new case (row) use dataSet.clearAllData();
		for(String[] dsData: data){
			String dsId = dsData[0];
			DataSet dataSet = model.createDataSet(dsId);
			for (int i = 1; i < dsData.length; i++) {
				String nodeId = headers[i];
				String observation = dsData[i];
				if (observation.trim().isEmpty()){
					// No observation
					continue;
				}
				dataSet.setObservation(net.getNode(nodeId), observation);
			}
		}
		
		// Calulate all networks and all data sets
		model.calculate();
		
		Node hasBronchitis = net.getNode("B");
		Node hasLungCancer = net.getNode("L");
		Node hasTuberculosis = net.getNode("T");
		
		String out = String.join(separatorOut, new String[]{"Case", "Has Bronchitis", "Has Lung Cancer", "Has Tuberculosis"}) + "\n";
		
		for(DataSet ds: model.getDataSets().values()){
			String outLine = String.join(separatorOut, Arrays.asList(
					ds.getId(),
					ds.getCalculationResult(hasBronchitis).getResultValue("yes").getValue()+"",
					ds.getCalculationResult(hasLungCancer).getResultValue("yes").getValue()+"",
					ds.getCalculationResult(hasTuberculosis).getResultValue("yes").getValue()+""
			));
			out += outLine + "\n";
		}
		
		Files.write(Paths.get("BatchCalculate.csv"), out.getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
	}
}
