# ProgineerTechAssignment

Vala assignment project has two services. It can be tested using RestClient as follows:
 
1- Places service using http://localhost:8080/places , which is a GET service that takes two parameters input and country and returns the description of the first five places provided by Places Google API.
 
 Example:
 Request: http://localhost:8080/places?input=dublin&country=uk 
 Response: 
			PLACES RESOURCE: user parameters are 
			'dublin'  and  'uk'

			Dublin, Ireland
			Dublin, CA, USA
			Dublin, OH, USA
			Dublin Airport (DUB), Dublin, Ireland
			Dublin Boulevard, Dublin, CA, USA
 
2- Metrics service that shows that returns total number of request made to the server and the total number of request made.

 Example:
 Request: http://localhost:8080/metrics
 Response: 
		METRIC RESOURCES

		{usa=5, egypt=1, uk=3, jordan=3, South=1}
		TOTAL NUMBER OF REQUESTS: 13