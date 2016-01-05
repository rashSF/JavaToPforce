package p4actions;

import java.net.URISyntaxException;
import java.util.List;

import com.perforce.p4java.exception.AccessException;
import com.perforce.p4java.exception.ConfigException;
import com.perforce.p4java.exception.ConnectionException;
import com.perforce.p4java.exception.NoSuchObjectException;
import com.perforce.p4java.exception.RequestException;
import com.perforce.p4java.exception.ResourceException;
import com.perforce.p4java.impl.generic.client.ClientView;

import p4api.P4Handler;

public class ConnectToPerforce {

	public static void main(String[] args) throws NoSuchObjectException, ConfigException, ResourceException, AccessException, RequestException, URISyntaxException {
		try {
			P4Handler.connect("p4java://localhost:1666", "rt", "rt@salesforce.com"); 
			System.out.println("Connected");
			ClientView clientView = P4Handler.getDepotView("rt_RASHMT1-LTR_9098");
			List<com.perforce.p4java.client.IClientViewMapping> viewList = clientView.getEntryList();
			String file = null;
			for(com.perforce.p4java.client.IClientViewMapping mapping : viewList){
				System.out.println("view list : "+mapping.getClient()+" ");
				file = mapping.getLeft();
			}
			
			P4Handler.getPerforceUsers();
			
			/*
			
			
			//Create workspace
			P4Handler.createCopyWorkspace("rt_RASHMT1-LTR_9098_1", "rt_RASHMT1-LTR_9098", "C:\\Users\\rt\\Documents\\New Salesforce Laptop\\New folder\\P4 Java to Perforce");
			//Sync file
			file = "c:\\Users\\rt\\Documents\\New Salesforce Laptop\\PERFORCE FOR SALESFORCE\\workspace\\src\\classes\\ID_AP_SchRetryFailures.cls";
			System.out.println("file details : "+file);
			P4Handler.syncFile(P4Handler.getClient("rt_RASHMT1-LTR_9098_1"), file, false);

			//Create pending change list
			P4Handler.checkOutFile(P4Handler.getClient("rt_RASHMT1-LTR_9098_1"), file);
			//check out file in  pending change list number 5
			P4Handler.checkOutFile(P4Handler.getClient("rt_RASHMT1-LTR_9098_1"), file, 5);
			
			P4Handler.addViewToWorkspace("rt_RASHMT1-LTR_9098_1", "//depot/overview-summary.html", "//a.rt_RASHMT1-LTR_9098_1/overview-summary.html", EntryType.INCLUDE);
			
			*/
		} catch (ConnectionException  e) {
			e.printStackTrace();
		}
		finally{
			P4Handler.disconnect();
		}
	}
}
