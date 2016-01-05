package p4api;

import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.client.IClientViewMapping;
import com.perforce.p4java.client.IClientSummary.IClientOptions;
import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.IMapEntry.EntryType;
import com.perforce.p4java.core.IUserSummary;
import com.perforce.p4java.core.file.FileSpecBuilder;
import com.perforce.p4java.exception.AccessException;
import com.perforce.p4java.exception.ConfigException;
import com.perforce.p4java.exception.ConnectionException;
import com.perforce.p4java.exception.NoSuchObjectException;
import com.perforce.p4java.exception.RequestException;
import com.perforce.p4java.exception.ResourceException;
import com.perforce.p4java.impl.generic.client.ClientOptions;
import com.perforce.p4java.impl.generic.client.ClientView;
import com.perforce.p4java.impl.generic.client.ClientView.ClientViewMapping;
import com.perforce.p4java.impl.generic.core.Changelist;
import com.perforce.p4java.impl.mapbased.client.Client;
import com.perforce.p4java.server.IServer;
import com.perforce.p4java.server.IServerInfo;
import com.perforce.p4java.server.ServerFactory;

public class P4Handler {

	private P4Handler(){} 
	
	private static IServer server = null;
	
	/*******************Connection functions*******************/
	public static synchronized void connect(String connectionString, String userName, String password) throws ConnectionException, 
														NoSuchObjectException, ConfigException, ResourceException, URISyntaxException, AccessException, RequestException{
		if(P4Handler.server != null) throw new ResourceException("Tool already in use");
		P4Handler.server = ServerFactory.getServer(connectionString, null); 
		P4Handler.server.connect();
		IServerInfo info = P4Handler.server.getServerInfo();
		System.out.println("server address: " +
		info.getServerAddress() + "\n"
		+ "server version" + info.getServerVersion() + "\n"
		+ "client address: " + info.getClientAddress() +
		"\n"
		+ "client working directory: " +
		info.getClientCurrentDirectory() + "\n"
		+ "client name: " + info.getClientName() + "\n"
		+ "user name: " + info.getUserName());

		P4Handler.server.setUserName(userName);
		P4Handler.server.login(password);
	}
	
	public static void disconnect(){
		try {
			if(P4Handler.server != null){
				P4Handler.server.logout();
				P4Handler.server.disconnect();
			}
		} catch (ConnectionException e) {
			e.printStackTrace();
		} catch (RequestException e) {
			e.printStackTrace();
		} catch (AccessException e) {
			e.printStackTrace();
		} catch (ConfigException e) {
			e.printStackTrace();
		}
	}
	
	/*******************Workspace related functions****************/
	public static ClientView getDepotView(String workspace) throws ConnectionException, RequestException, AccessException{
		IClient client = P4Handler.server.getClient(workspace);
		return client.getClientView();
	}
	
	public static IClient getClient(String workspace) throws ConnectionException, RequestException, AccessException{
		return P4Handler.server.getClient(workspace);
	}
	
	public static synchronized boolean isWorkspacePresent(String workspace) throws ConnectionException, RequestException, AccessException{
		if(null == P4Handler.server.getClientTemplate(workspace)) return true;
		return false;
	}
	
	public static synchronized void createCopyWorkspace(String refWorkspaceName, String newWorkspaceName, String root) 
			throws ConnectionException, RequestException, AccessException, UnknownHostException{
		if(isWorkspacePresent(refWorkspaceName) && !isWorkspacePresent(newWorkspaceName)){
			ClientView refView = P4Handler.getDepotView(refWorkspaceName);
			IClient newClient = new Client();
			newClient.setName(newWorkspaceName);
			newClient.setRoot(root);
			newClient.setServer(P4Handler.server);
			newClient.setOwnerName(P4Handler.server.getUserName());
			newClient.setHostName(InetAddress.getLocalHost().getHostName());
			IClientOptions newClientOptions = new ClientOptions(true, false, false, false, false, true);
			newClient.setOptions(newClientOptions);
			//P4Handler.server.setCurrentClient(newClient);
			ClientView newView = new ClientView();
			List<IClientViewMapping> refMapping = refView.getEntryList();
			for(IClientViewMapping mapping : refMapping){
				ClientViewMapping newMap = new ClientViewMapping();
				newMap.setLeft(mapping.getLeft());
				newMap.setRight(mapping.getRight().replace(refWorkspaceName, newWorkspaceName));
				newMap.setType(mapping.getType());
				newView.addEntry(newMap);
			}
			newClient.setClientView(newView);
			P4Handler.server.createClient(newClient);
		}
	}
	
	public static synchronized void syncFile(IClient workspace, String file, boolean forceSync) throws ConnectionException, RequestException, AccessException{
		P4Handler.server.setCurrentClient(workspace);
		workspace.sync(FileSpecBuilder.makeFileSpecList(Arrays.asList(file)), forceSync, false, false, false);
	}
	
	public static synchronized void addViewToWorkspace(IClient workspace, ClientViewMapping newMapping) 
			throws ConnectionException,RequestException, AccessException{
		workspace.getClientView().addEntry(newMapping);
		P4Handler.server.updateClient(workspace);
	}
	
	public static void addViewToWorkspace(String workspace, String leftPath, String rightPath, EntryType type) 
			throws ConnectionException, RequestException, AccessException{
		ClientViewMapping mapping = new ClientViewMapping();
		mapping.setLeft(leftPath);
		mapping.setRight(rightPath);
		mapping.setType(type);
		addViewToWorkspace(P4Handler.server.getClient(workspace), mapping);
	}
	
	/*******************CL related functions********************/
	public static synchronized void checkOutFile(IClient workspace, String file) 
			throws ConnectionException, RequestException, AccessException{
		P4Handler.server.setCurrentClient(workspace);
		IChangelist CL = new Changelist();
		CL.setServer(P4Handler.server);
		CL = workspace.createChangelist(CL);
		workspace.editFiles(FileSpecBuilder.makeFileSpecList(Arrays.asList(file)), false, false, CL.getId(), null);
	}
	
	public static synchronized void checkOutFile(IClient workspace, String file, int clId) 
			throws ConnectionException, RequestException, AccessException{
		P4Handler.server.setCurrentClient(workspace);
		IChangelist CL = new Changelist();
		CL.setId(clId);
		CL.setServer(P4Handler.server);
		workspace.editFiles(FileSpecBuilder.makeFileSpecList(Arrays.asList(file)), false, false, CL.getId(), null);
	}
	
	/******************** Invoking P4Admin Client Methods 
	 * @throws AccessException 
	 * @throws RequestException 
	 * @throws ConnectionException ************************/
	public static void getPerforceUsers() throws ConnectionException, RequestException, AccessException{
		
		List<IUserSummary> lstUsers = P4Handler.server.getUsers(null, 100);
		if(!lstUsers.isEmpty()){
			for(IUserSummary us : lstUsers){
				System.out.println("I summary user Email  "+us.getEmail()+" Full name "+us.getFullName());
			}
		}
		
	}
}
