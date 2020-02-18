import java.util.List;

public class ItemController {

	private FridgeDSC fridgeDSC;

	public ItemController(String dbHost, String dbUserName, String dbPassword) throws Exception {
		fridgeDSC = new FridgeDSC(dbHost, dbUserName, dbPassword);

		try {
			fridgeDSC.connect();
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	public List<Item> get() throws Exception {
		return fridgeDSC.getAllItems();
	}

	// To perform some quick tests
	public static void main(String [] args) throws Exception {
		try {
			ItemController ic = new ItemController("latcs7.cs.latrobe.edu.au:3306", "18938149", "HfBaX3XJFC44FNcmNhkR");
			//testing
			System.out.println(gc.get());

		} catch (Exception exp) {
			exp.printStackTrace();
		}

	}
}