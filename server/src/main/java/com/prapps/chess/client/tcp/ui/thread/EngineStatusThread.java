package com.prapps.chess.client.tcp.ui.thread;

import java.io.IOException;

import javax.swing.table.DefaultTableModel;

import com.prapps.chess.client.tcp.cb.ServerConnector;
import com.prapps.chess.client.tcp.cb.ServerDetails;
import com.prapps.chess.server.uci.thread.AbstractRunnable;
import com.prapps.chess.server.uci.thread.State;
import com.prapps.chess.uci.share.ProtocolConstants;

public class EngineStatusThread extends AbstractRunnable {

	private DefaultTableModel tableModel;
	
	public EngineStatusThread(DefaultTableModel tableModel) {
		this.tableModel = tableModel;
		setState(State.New);
	}
	
	public void run() {
		ServerConnector connector = null;
		try {
			connector = ServerConnector.getInstance();
			setState(State.Running);
			//while(!(State.Stopping == getState() || State.Closed == getState())) {
			
				String servers = connector.sendMsg(ProtocolConstants.GET_ENGINE_STATUSES);
				LOG.fine("server statuses: " +servers);
				connector.close();
				ServerDetails serverDetails = new ServerDetails(servers);
				while(tableModel.getRowCount() > 0) {
					tableModel.removeRow(0);
				}	
				for(String[] server : serverDetails.getServers()) {
					tableModel.addRow(server);
				}
				setState(State.Waiting);
				//Thread.sleep(60000);
			//}
		} catch (IOException e) {
			e.printStackTrace();
		} /*catch (InterruptedException e) {
			e.printStackTrace();
		}*/
		finally {
			try {
				setState(State.Closed);
				if(connector.isConnected()) {
					connector.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
