package yelp;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.util.Iterator;
import java.util.Vector;

/**
 * Code sample for accessing the Yelp API V2.
 * 
 * This program demonstrates the capability of the Yelp API version 2.0 by using the Search API to
 * query for businesses by a search term and location, and the Business API to query additional
 * information about the top result from the search query.
 * 
 * <p>
 * See <a href="http://www.yelp.com/developers/documentation">Yelp Documentation</a> for more info.
 * 
 */
public class YelpAPI {
  static String search = getSearch();
  static String location = getLocation();
  
	//static String location = getLocation();
  private static final String API_HOST = "api.yelp.com";
  private static final String DEFAULT_TERM = search;
  private static final String DEFAULT_LOCATION = location;
  private static final int SEARCH_LIMIT = 20;
  private static final String SEARCH_PATH = "/v2/search";
  private static final String BUSINESS_PATH = "/v2/business";

  /*
   * Update OAuth credentials below from the Yelp Developers API site:
   * http://www.yelp.com/developers/getting_started/api_access
   */
  private static final String CONSUMER_KEY = "aQsH6JSTMxT_REbxq5Idlw";
  private static final String CONSUMER_SECRET = "7wEtMdch2Px4XXkr-B3T_F9s6bo";
  private static final String TOKEN = "mFuE_f_YftsDIxEX6uIXeTCjTK_LHdOp";
  private static final String TOKEN_SECRET = "9zNgAL6tsvmNor4FGp4tl2EdihA";

  OAuthService service;
  Token accessToken;

  /**
   * Setup the Yelp API OAuth credentials.
   * 
   * @param consumerKey Consumer key
   * @param consumerSecret Consumer secret
   * @param token Token
   * @param tokenSecret Token secret
   */
  public YelpAPI(String consumerKey, String consumerSecret, String token, String tokenSecret) {
    this.service =
        new ServiceBuilder().provider(TwoStepOAuth.class).apiKey(consumerKey)
            .apiSecret(consumerSecret).build();
    this.accessToken = new Token(token, tokenSecret);
  }

  /**
   * Creates and sends a request to the Search API by term and location.
   * <p>
   * See <a href="http://www.yelp.com/developers/documentation/v2/search_api">Yelp Search API V2</a>
   * for more info.
   * 
   * @param term <tt>String</tt> of the search term to be queried
   * @param location <tt>String</tt> of the location
   * @return <tt>String</tt> JSON Response
   */
  public String searchForBusinessesByLocation(String term, String location) {
    OAuthRequest request = createOAuthRequest(SEARCH_PATH);
    request.addQuerystringParameter("term", term);
    request.addQuerystringParameter("location", location);
    request.addQuerystringParameter("limit", String.valueOf(SEARCH_LIMIT));
    return sendRequestAndGetResponse(request);
  }

  /**
   * Creates and sends a request to the Business API by business ID.
   * <p>
   * See <a href="http://www.yelp.com/developers/documentation/v2/business">Yelp Business API V2</a>
   * for more info.
   * 
   * @param businessID <tt>String</tt> business ID of the requested business
   * @return <tt>String</tt> JSON Response
   */
  public String searchByBusinessId(String businessID) {
    OAuthRequest request = createOAuthRequest(BUSINESS_PATH + "/" + businessID);
    return sendRequestAndGetResponse(request);
  }

  /**
   * Creates and returns an {@link OAuthRequest} based on the API endpoint specified.
   * 
   * @param path API endpoint to be queried
   * @return <tt>OAuthRequest</tt>
   */
  private OAuthRequest createOAuthRequest(String path) {
    OAuthRequest request = new OAuthRequest(Verb.GET, "http://" + API_HOST + path);
    return request;
  }

  /**
   * Sends an {@link OAuthRequest} and returns the {@link Response} body.
   * 
   * @param request {@link OAuthRequest} corresponding to the API request
   * @return <tt>String</tt> body of API response
   */
  private String sendRequestAndGetResponse(OAuthRequest request) {
    //System.out.println("Querying " + request.getCompleteUrl() + " ...");
    this.service.signRequest(this.accessToken, request);
    Response response = request.send();
    return response.getBody();
  }

  /**
   * Generates random number in range of vector size. Used to determine which result to display
   */
  public static int randInt(int min, int max)
  {
	  Random rand = new Random();
	  int randomNum = rand.nextInt((max-min)+1)+min;
	  return randomNum;
  }
  /**
   * Queries the Search API based on the command line arguments and takes the first result to query
   * the Business API.
   * 
   * @param yelpApi <tt>YelpAPI</tt> service instance
   * @param yelpApiCli <tt>YelpAPICLI</tt> command line arguments
   */
  private static void queryAPI(YelpAPI yelpApi, YelpAPICLI yelpApiCli) {
    String searchResponseJSON =
        yelpApi.searchForBusinessesByLocation(yelpApiCli.term, yelpApiCli.location);

    JSONParser parser = new JSONParser();
    JSONObject response = null;
    try {
      response = (JSONObject) parser.parse(searchResponseJSON);
    } catch (ParseException pe) {
      System.out.println("Error: could not parse JSON response:");
      System.out.println(searchResponseJSON);
      System.exit(1);
    }
    
    Scanner in = new Scanner(System.in);
    System.out.print("Enter how many results: ");
    int total = in.nextInt();
    JSONArray businesses = (JSONArray) response.get("businesses");
    List<String> IDString = new ArrayList<String>(total);
    List<String> rating = new ArrayList<String>(total);
    List<String> reviewCount = new ArrayList<String>(total);
    List<String> location = new ArrayList<String>(total);
    
    List listName = new LinkedList();
    List listRating = new LinkedList();
    for (int i = 0; i < total ; i++)
    {

	    JSONObject firstBusiness = (JSONObject) businesses.get(i);
	    String firstBusinessID = firstBusiness.get("id").toString();
	    IDString.add(firstBusinessID);
	    String businessRating = firstBusiness.get("rating").toString();
	    rating.add(businessRating);
	    String numberOfRating = firstBusiness.get("review_count").toString();
	    reviewCount.add(numberOfRating);
	    //System.out.println(IDString.get(i));
	    listName.add(IDString.get(i));
	    System.out.println(listName.get(i));
	    //System.out.println(rating.get(i) + " with " + reviewCount.get(i) + " ratings");
	    listRating.add(rating.get(i) + " with " + reviewCount.get(i) + " ratings");
	    System.out.println(listRating.get(i));
	}
    
    //create linked list to store list of numbers. remove when rand function chooses number
    List listA = new LinkedList();
    for (int j = 1; j <= total; j++)
    {
    	listA.add(j);
    }
    int keepgoing;
    int curr;
    do
    {
    	curr = randInt(0,listA.size()-1);
    	System.out.println("current random number: " + curr);
    	String pos = listA.get(curr).toString();
    	int num = Integer.parseInt(pos);
    	System.out.println(listName.get(num));
    	System.out.println(listRating.get(num));
    	listA.remove(curr);
    	System.out.println("eat here? (0 to display next/1 to select): ");
    	Scanner in2 = new Scanner(System.in);
    	keepgoing = in2.nextInt();
    	if (keepgoing == 1)
    	{
    		break;
    	}
    	if (listA.isEmpty())
    	{
    		break;
    	}
    }
    while (true);
	System.out.println("Enjoy!");
    //print info for the current business
     
    
 }
  
  /**
   * Command-line interface for the sample Yelp API runner.
   */
  private static class YelpAPICLI {
    @Parameter(names = {"-q", "--term"}, description = "Search Query Term")
    public String term = DEFAULT_TERM;

    @Parameter(names = {"-l", "--location"}, description = "Location to be Queried")
    public String location = DEFAULT_LOCATION;
  }

  /**
   * Main entry for sample Yelp API requests.
   * <p>
   * After entering your OAuth credentials, execute <tt><b>run.sh</b></tt> to run this example.
   */
  public static void main(String[] args) {
    YelpAPICLI yelpApiCli = new YelpAPICLI();
    new JCommander(yelpApiCli, args);

    YelpAPI yelpApi = new YelpAPI(CONSUMER_KEY, CONSUMER_SECRET, TOKEN, TOKEN_SECRET);
    queryAPI(yelpApi, yelpApiCli);
  }
  
  /**
   *  Get user input for what term to search for.
   * @return
   */
  public static String getSearch()
  {
	  String search;
	  Scanner in = new Scanner(System.in);
	  System.out.print("Enter search term: ");
	  search = in.nextLine();
	  return search;	  
  }
  
  /**
   * Get user input for location
   * @return
   */
  public static String getLocation()
  {
	  String loc;
	  Scanner in = new Scanner(System.in);
	  System.out.println("Enter location (city, state): ");
	  loc = in.nextLine();
	  return loc;
  }
   

}