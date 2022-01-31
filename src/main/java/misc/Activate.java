package misc;

import uk.co.agena.minerva.util.product.License;

/**
 * This is an alternative way to activate your license if you can't do it via CLI
 * @author Eugene
 */
public class Activate {
	public static void main(String[] args) {
		License.keyActivate("1234-ABCD-5678-EXA-MPLE");
	}
}
