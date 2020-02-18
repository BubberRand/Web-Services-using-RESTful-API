import java.io.*;
import java.lang.reflect.*;

import javax.servlet.*;
import javax.servlet.http.*;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

public class FridgeRouterServlet extends HttpServlet {
	public static final String CONTENT_TYPE = "application/json";
	public static final String CHARACTER_ENCODING = "utf-8";

	// these constants are also available at javax.ws.rs.HttpMethod
	// but it does not define PATCH, we we are creating our own.
	// more: https://www.restapitutorial.com/lessons/httpmethods.html
	public static final String HTTP_GET = "GET";
	public static final String HTTP_POST = "POST";
	public static final String HTTP_PUT = "PUT";
	public static final String HTTP_DELETE = "DELETE";

	// status codes: http://www.informit.com/articles/article.aspx?p=29817&seqNum=7
	// more https://www.restapitutorial.com/lessons/httpmethods.html
	// we will be using those defined in HttpServletResponse
	// they are listed at https://tomcat.apache.org/tomcat-7.0-doc/servletapi/javax/servlet/http/HttpServletResponse.html
	// more: https://www.restapitutorial.com/httpstatuscodes.html

	public static final String CONTROLLER_STR = "Controller";

	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// sets the response CONTENT_TYPE and CHARACTER_ENCODING
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		Object responseObj = null;

		// grabs the path info from HttpServletRequest argument. See how you can get path from incoming request
        String pathInfo = request.getPathInfo();

        // grabs the http method from HttpServletRequest argument. See how you can get incoming request method
        String httpMethod = request.getMethod();

        // pathInfo will be in format: /{resource-name}/{query-string}
        // we want resource-name; we split on "/" and take the
        // second occurence, which is array position 1 of split("/");
        // any third occurence would be a model id or a search query parameter
        String pathInfoArray[] = pathInfo.split("/");

		try {
			// pathInfo has to have at least a resource-name, which is at array
			// location 1 in pathInfoArray - if no resource-name found throws error
	        if (pathInfoArray.length <= 1)
	        	throw new MissingArgumentException("Resource target not defined.");

	        // the model is needed for json parsing purposes using Gson library
	        // following uppercase-camel-case convention for class naming, that is
	        // - resource-name grocery will become class Grocery, or
	        // - resource-name GroCerY will become class Grocery, or ...

	        // from pathInfoArray, grab the modelName. Model name would be passed on in request after /api/
	        String modelName = pathInfoArray[1];

			// make the modelName first character uppercase and all other characters lowercase
			modelName = modelName.substring(0, 1).toUpperCase() + modelName.substring(1).toLowerCase(); 

	        // the controller is needed for the matching action defined by the http method
	        String controllerName = String.join("", modelName, CONTROLLER_STR);

	        // we then use Java Reflection to find Model and Controller,
	        // 		example: if resource-name is "groceRy", matching
	        //		model class is Grocery and matching controller class
	        //		is GroceryController

	        // find the controllerClass using String controllerName,
	        Class<?> controllerClass = Class.forName(controllerName); 

	        // find the modelClass using String modelName
	        Class<?> modelClass = Class.forName(modelName);

			// getting database config info from web.xml, putting the info in
			// a string array
	        String[] dbConfig = new String[3];
	       	dbConfig[0] = getServletContext().getInitParameter("dbhost");
			    dbConfig[1] = getServletContext().getInitParameter("dbusername");
			    dbConfig[2] = getServletContext().getInitParameter("dbpassword");

			// creating an instance of controllerClass; NOTE that the next 2 lines finds
			// the matching constructor (with 3 String arguments) and instantiate the
			// class using that constructor passing in String array dbConfig of length 3
			Constructor constructor = controllerClass.getConstructor(
				new Class[] {String.class, String.class, String.class}
			);

		Object controllerInstance = constructor.newInstance((Object[]) dbConfig);

			int modelId = 0;
			Method method = null;
			switch (httpMethod) {
				case HTTP_GET:
					// if pathInfoArray has 3rd argument (our design denotes that
					// any 3rd argument for an HTTP GET is the relevant model id)
					// then find the matching controllerClass method get(int id)
					// NOTE: the 3rd argument is sent as a String and needs to be
					// parsed as our model id is of type int.
					if (pathInfoArray.length >= 3) {

						// find the modelId in pathInfoArray (don't forget to parse to int)
						// Your grocery item will be 3rd element in the array
						modelId = Integer.parseInt(pathInfoArray[2]); 
						method = controllerClass.getMethod("get", int.class);

						responseObj = method.invoke(controllerInstance, modelId);

						if (responseObj == null)
							throw new ResourceNotFoundException(modelName + " with id " + modelId + " not found!");
					}
					// else find the matching controllerClass method get()
					else {

						// identify method get with no id
						method = controllerClass.getMethod("get");

						// invoke method on controllerInstance
						responseObj = method.invoke(controllerInstance);
					}
					break;
				case HTTP_POST: // NOTE: this case is given fully complete; it is the most complex part; use it as a reference example
					// grab post data
					String resourceData = buildResourceData(request);

					// find relevant add method in controllerClass
					method = controllerClass.getMethod("add", modelClass);

					// invoke method with parse (converted from JSON to modelClass) post data
					Object id = method.invoke(controllerInstance, new Gson().fromJson(resourceData, modelClass));

          Map<String, String> message = new HashMap<String, String>();

          if((int)id == -1){
            throw new ResourceNotFoundException("Quantity can't be 0");
          }
          else {
            message.put("message", "created ok! " + modelName + " id " + Integer.parseInt(id.toString()));
            responseObj = message;
          }
					break;
				case HTTP_PUT:
					if (pathInfoArray.length >= 3) {

						// find the modelId in pathInfoArray (don't forget to parse to int)
						modelId = Integer.parseInt(pathInfoArray[2]);

						// identify method get with id
						method = controllerClass.getMethod("update", int.class);

						// invoke method on controllerInstance, passing modelId
						responseObj = method.invoke(controllerInstance, modelId);

						if (responseObj == null)
							throw new ResourceNotFoundException(modelName + " with id " + modelId + " not found! Cannot Update!");
					} else
						throw new MissingArgumentException("Attribute id required! Cannot Update!");
					break;
				case HTTP_DELETE:
					if (pathInfoArray.length >= 3) {

						// find the modelId in pathInfoArray (don't forget to parse to int)
						modelId = Integer.parseInt(pathInfoArray[2]); 

						// identify method get with id
						method = controllerClass.getMethod("delete", int.class); 

						// invoke method on controllerInstance, passing modelId
						responseObj = method.invoke(controllerInstance, modelId); 

						if (Integer.parseInt(responseObj.toString()) <= 0)
							throw new ResourceNotFoundException(modelName + " with id " + modelId + " not found! Cannot Delete!");

						responseObj = buildMessage(modelName + " with id " + modelId + " deleted!");
					} else
						throw new MissingArgumentException("Attribute id required! Cannot Delete!");
					break;
				default:
					// we do not provide action for any other HTTP methods
					throw new NoSuchMethodException();
			} // switch

			response.getWriter().print(new Gson().toJson(responseObj));
		}
		catch (Exception exp) {
			String message = exp.getMessage();
			// setting default error status code
			response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);

			if (exp instanceof ResourceNotFoundException)
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);

			if (exp instanceof MissingArgumentException)
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);

			if (exp instanceof ClassNotFoundException) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				message = "Resource not found!";
			}

			if (exp instanceof NoSuchMethodException) {
				response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
				message = "Method not allowed on specified resource!";
			}

			if (exp instanceof InvocationTargetException) {
				// identify instanceof UpdateNotAllowedException exception
					// set response status to SC_METHOD_NOT_ALLOWED
					// set message to intercepted exception
					// see provided Validation Framework Validator class (exception handling part) for how to
				if (exp.getCause() instanceof UpdateNotAllowedException)
				{
					response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
					message = "Update Not Allowed!";
				}
				// identify instanceof ValidationException exception
					// set response status to SC_PRECONDITION_FAILED
					// set message to intercepted exception
					// see provided Validation Framework Validator class (exception handling part) for how to
          
				if (exp.getCause() instanceof ValidationException)
				{
					response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
					message = "Not a Valid Grocery!";
				}

			}

			response.getWriter().write(new Gson().toJson(buildMessage(message)));
		}

	}


	// HELPER METHODS
    private String buildResourceData(HttpServletRequest request) throws Exception {
        // request has post/put data
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } finally {
            reader.close();
        }
        return sb.toString();
    }

	private Map buildMessage(String msg) {
		Map<String, String> message = new HashMap<String, String>();
		message.put("message", msg);
		return message;
	}

}
