import java.util.List;

public class GroceryController {

	private FridgeDSC fridgeDSC;

	public GroceryController(String dbHost, String dbUserName, String dbPassword) throws Exception {
		fridgeDSC = new FridgeDSC(dbHost, dbUserName, dbPassword);

		try {
			fridgeDSC.connect();
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	public List<Grocery> get() throws Exception {
		return fridgeDSC.getAllGroceries();
	}

	public Grocery get(int id) throws Exception {
		return fridgeDSC.searchGrocery(id);
	}

	public int add(Grocery g) throws Exception {
    // validate argument g, using Validation Framework
    try{
      Validator.validate(g);
    }
    catch( ValidationException e )
    {
        e.getMessage();
        return -1;
    }

      // make a relevant call to a fridgeDSC method
      // this method should return the id of the newly created grocery

      return fridgeDSC.addGrocery(g.getItem().getName(), g.getQuantity(), g.getSection());
	}

	public Grocery update(int id) throws Exception {
    // make a relevant call to a fridgeDSC method
    return fridgeDSC.useGrocery(id);

		// this method should return the updated grocery object
	}

	public int delete(int id) throws Exception {
    // make a relevant call to a fridgeDSC method
    

		// this method should return what ever the fridgeDSC method call returns
		return fridgeDSC.removeGrocery(id);
	}

	// To perform some quick tests
	public static void main(String [] args) throws Exception {
		// testing each of the above methods here
		try {
			// CHANGE the username and password to match yours
			// CHANGE the first param to your database host if you are not using latcs7
			GroceryController gc = new GroceryController("latcs7.cs.latrobe.edu.au:3306", "18938149", "HfBaX3XJFC44FNcmNhkR");
			/* UNCOMMENT the following and add the relevant parameters/arguments to do your testing
			System.out.println(gc.get());
			System.out.println(gc.get(...
				// some id that exists in your grocery table
				)
			);
			System.out.println(gc.add(new Grocery(...)));
			System.out.println(gc.update(...
				// some id that exists in your grocery table
				)
			);
			System.out.println(gc.delete(...
				// some id that exists in your grocery table
				)
			);
			*/

		} catch (Exception exp) {
			exp.printStackTrace();
		}

	}
}
