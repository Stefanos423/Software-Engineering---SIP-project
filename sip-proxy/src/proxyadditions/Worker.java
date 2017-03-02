package proxyadditions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class Worker implements Runnable {

	protected Socket clientSocket = null;
	protected String serverText = null;
	
	public Worker(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}
	
	public void run() {
		
		try {
			DataInputStream in = new DataInputStream(clientSocket.getInputStream());
			DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
			
			String input = in.readUTF();
			String parts[] = input.split(" ");
			String message = parts[0];
			String username = parts[1];
			
			switch (message) {
			case "register": {
				String password = parts[2];
				if (RegistrarServer.get().registerUser(username, password))
					out.writeUTF("ok");
				else
					out.writeUTF("register_error");
				break;
			}
			case "authenticate": {
				String password = parts[2];
				if (RegistrarServer.get().authenticateUser(username, password))
					out.writeUTF("ok");
				else
					out.writeUTF("authenticate_error");
				break;
			}
			case "set_rate": {
				int rate = Integer.parseInt(parts[2]);
				if (RegistrarServer.get().setRate(username, rate))
					out.writeUTF("ok");
				else
					out.writeUTF("set_rate_error");
				break;
			}
			case "log": {
				int id = DatabaseServer.get().getUserId(username);
				List<CallProperties> list = TimerServer.get().getCallsList(id);
				for (CallProperties prop: list) {
					out.writeUTF(prop.toString());
				}
				break;
			}
			case "ping":
				LocationServer.get().userPinged(username);
			case "get_user_id":
				out.writeUTF(String.valueOf(
						DatabaseServer.get().getUserId(username)));
				break;
			case "get_balance":
				out.writeUTF(String.valueOf(
						RegistrarServer.get().getUserBalance(username)));
				break;
			case "get_rate":
				out.writeUTF(String.valueOf(
						DatabaseServer.get().getUserRate(
								DatabaseServer.get().getUserId(username))));
				break;
			case "get_forward": {
				int id = DatabaseServer.get().getUserId(username);
				int forwardee = ForwardingServer.get().getForwardeeId(id);
				if (forwardee == 0)
					out.writeUTF("<nobody>");
				else
					out.writeUTF(DatabaseServer.get().getUserUsername(forwardee));
				break;
			}
			case "get_last_recipient": {
				int callerId = DatabaseServer.get().getUserId(username);
				int calleeId = DatabaseServer.get().getUserId(parts[2]);
				if (callerId < 0 || calleeId < 0) {
					out.writeUTF("<no_such_user>");
					break;
				}
				int lastId = ForwardingServer.get().getLastCallRecipient(callerId, calleeId);
				if (lastId < 0)
					out.writeUTF("<cycle_detected>");
				else
					out.writeUTF(DatabaseServer.get().getUserUsername(lastId));
				break;
			}
			case "is_online":
				if (LocationServer.get().isUserOnline(username))
					out.writeUTF("yes");
				else
					out.writeUTF("no");
				break;
			case "forward": {
				String forwardee = parts[2];
				int uid = DatabaseServer.get().getUserId(username);
				int fid = DatabaseServer.get().getUserId(forwardee);
				if (ForwardingServer.get().addForwarding(uid, fid))
					out.writeUTF("ok");
				else
					out.writeUTF("forward_error");
				break;
			}
			case "stop_forward": {
				int uid = DatabaseServer.get().getUserId(username);
				ForwardingServer.get().removeForwarding(uid);
				out.writeUTF("ok");
				break;
			}
			case "blocked_users": {
				int id = DatabaseServer.get().getUserId(username);
				List<String> list = BlockingServer.get().getBlockedUsers(id);
				for (String item: list)
					out.writeUTF(item);
				break;
			}
			case "unblock": {
				String target = parts[2];
				int uid = DatabaseServer.get().getUserId(username);
				int tid = DatabaseServer.get().getUserId(target);
				BlockingServer.get().unblock(uid, tid);
				break;
			}
			case "unblock_all": {
				int uid = DatabaseServer.get().getUserId(username);
				BlockingServer.get().unblockAll(uid);
				break;
			}
			case "block": {
				String target = parts[2];
				int uid = DatabaseServer.get().getUserId(username);
				int tid = DatabaseServer.get().getUserId(target);
				if (BlockingServer.get().block(uid, tid))
					out.writeUTF("ok");
				else
					out.writeUTF("block_error");
				break;
			}
			case "is_blocked": {
				String target = parts[2];
				int uid = DatabaseServer.get().getUserId(username);
				int tid = DatabaseServer.get().getUserId(target);
				if (BlockingServer.get().isBlocked(uid, tid))
					out.writeUTF("yes");
				else
					out.writeUTF("no");
				break;
			}
			case "start_call": {
				String callee = parts[2];
				int callerId = DatabaseServer.get().getUserId(username);
				int calleeId = DatabaseServer.get().getUserId(callee);
				TimerServer.get().start(callerId, calleeId);
				break;
			}
			case "end_call": {
				String callee = parts[2];
				int callerId = DatabaseServer.get().getUserId(username);
				int calleeId = DatabaseServer.get().getUserId(callee);
				int callId = DatabaseServer.get().getCallId(callerId, calleeId);
				if (callId == 0) return;
				else if (callId < 0) callId = -callId;
				TimerServer.get().stop(callId);
				break;
			}
			case "call_id": {
				String callee = parts[2];
				int callerId = DatabaseServer.get().getUserId(username);
				int calleeId = DatabaseServer.get().getUserId(callee);
				out.writeUTF(Integer.toString(DatabaseServer.get().getCallId(callerId, calleeId)));
				break;
			}
			}

			in.close();
			out.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
