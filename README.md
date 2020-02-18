# Web-Services-using-RESTful-API
This is an assignment I completed at University with the goal to design and implement a RESTful API that will provide an access endpoint to a data source
controller similar another of my repositories: [JavaFX-Application-using-JDBC](https://github.com/oreimu/JavaFX-Application-using-JDBC).
This RESTful API implements web services for making available resources regarding your fridge groceries and items using a combination of Java Reflection and Servlet API.

## System Files:

* The servlet descriptor file **web.xml**
* The jar files for JSON conversion and JDBC(MySQL) driver (/lib)
* The Java files for Validation framework
  * **Min.java**, **MinValidator.java**
  * **Max.java**, **MaxValidator.java**
  * **NotNull.java**, **NotNullValidator.java**
  * **CharCount.java**, **CharCountValidator.java**
  * **Validator.java**, **ValidatorException.java**
* The Data Source Controller, **FridgeDSC.java**
* The Models
  * **Grocery.java**
  * **Item.java**
* The Controllers
  * **GroceryController.java**
  * **ItemController.java**
* The Router Servlet
  * **FridgeRouterServlet.java**
* A few custom Exception sub-classes
  * **MissingArgumentException.java**
  * **ResourceNotFoundException.java**
  * **UpdateNotAllowedException.java**
* The SQL Script to create and populate database
  * **CreateDatabaseSQL.sql**  
