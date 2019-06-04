package example;

import com.agenarisk.api.exception.AdapterException;
import com.agenarisk.api.exception.ModelException;
import com.agenarisk.api.io.JSONAdapter;
import com.agenarisk.api.io.XMLAdapter;
import com.agenarisk.api.model.Model;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.stream.Collectors;

/**
 *
 * @author Eugene Dementiev
 */
public class Demo {
	
	public static void main(String[] args) throws Exception {
		String asiaXml = read("Asia.xml");
		Model model = loadXml(asiaXml);
		model.calculate();
		System.out.println(JSONAdapter.toJSONObject(model.getLogicModel()));
	}
	
	private static Model loadXml(String xml) throws ModelException, AdapterException{
		return Model.createModel(XMLAdapter.xmlToJson(xml));
	}
	
	private static String read(String fileName) throws IOException {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(Demo.class.getResourceAsStream(fileName), Charset.forName("UTF-8")))) {
			return br.lines().collect(Collectors.joining(System.lineSeparator()));
		}
	}
}

