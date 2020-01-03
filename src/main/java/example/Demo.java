package example;

import com.agenarisk.api.model.*;
import java.io.File;

/**
 *
 * @author Eugene Dementiev
 */
public class Demo {

	public static void main(String[] args) throws Exception {

		String modelFilePath = new File(Demo.class.getClassLoader().getResource("example/Asia.xml").getFile()).getAbsolutePath();

		// Load and calculate the model
		Model model = Model.loadModel(modelFilePath);
		model.calculate();

		// We only have one Data Set in the Network, so we can just pick first
		// Same for network
		DataSet ds = model.getDataSetList().get(0);
		Network net = model.getNetworkList().get(0);
		
		// We want to see the probability of "yes" for cancer, tuberculosis and bronchitis
		Node tuberculosis = net.getNode("T");
		Node cancer = net.getNode("L");
		Node bronchitis = net.getNode("B");
		
		System.out.println("Tuberculosis: " + ds.getCalculationResult(tuberculosis).getResultValue("yes").getValue());
		System.out.println("Cancer: " + ds.getCalculationResult(cancer).getResultValue("yes").getValue());
		System.out.println("Bronchitis: " + ds.getCalculationResult(bronchitis).getResultValue("yes").getValue());
		
		System.out.println("");
		
		// Now we enter some observations into the model and check results again
		
		// Smoker
		ds.setObservation(net.getNode("S"), "yes");
		
		// No Asia
		ds.setObservation(net.getNode("A"), "no");
		
		// Positive X-ray
		ds.setObservation(net.getNode("X"), "yes");
		
		model.calculate();
		
		System.out.println("Tuberculosis: " + ds.getCalculationResult(tuberculosis).getResultValue("yes").getValue());
		System.out.println("Cancer: " + ds.getCalculationResult(cancer).getResultValue("yes").getValue());
		System.out.println("Bronchitis: " + ds.getCalculationResult(bronchitis).getResultValue("yes").getValue());
		
		// As expected, probabilty of cancer has grown significantly

	}

}
