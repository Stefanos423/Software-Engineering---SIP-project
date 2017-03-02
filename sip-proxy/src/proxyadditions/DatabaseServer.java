package proxyadditions;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

public class DatabaseServer {
	private static DatabaseServer instance = null;
	
	private static final String HOST = "127.0.0.1";
	private static final int PORT = 3306;
	private static final String DATABASE = "sip_project_db";
	private static final String USER = "sip_user";
	private static final String PASS = "sip";
	private static final double DEFAULT_BALANCE = 100.0;
	private static final int DEFAULT_RATE = 0;
	
	private Connection connection;

	private DatabaseServer() {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static DatabaseServer get() {
		if (instance == null)
			instance = new DatabaseServer();
		return instance;
	}
	
	public void connect() throws SQLException {
		if (connection == null) {
			connection = DriverManager.getConnection(String.format(
					"jdbc:mysql://%s:%d/%s?user=%s&password=%s" +
					"&characterEncoding=UTF-8&characterSetResults=UTF-8",
					HOST, PORT, DATABASE, USER, PASS));
			
			repairUsers();
			repairCalls();
		}
	}

	public void disconnect() {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			connection = null;
		}
	}
	
	public boolean isConnected() {
		return connection != null;
	}
	
	private static void safeClose(AutoCloseable... objs) {
		for (AutoCloseable obj: objs) {
			try {
				if (obj != null)
					obj.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public boolean addUser(String username, byte[] pass, byte[] salt) {
		return addUser(username, pass, salt, DEFAULT_BALANCE, DEFAULT_RATE);
	}
	public boolean addUser(String username, byte[] pass, byte[] salt,  double balance, int rate) {
		PreparedStatement pqs = null;
		try {
			String command = "insert into Users values (NULL, ?, ?, ?, ?, ?, NULL, true)";
			pqs = connection.prepareStatement(command);
			pqs.setString(1, username);
			pqs.setBytes(2, pass);
			pqs.setBytes(3, salt);
			pqs.setDouble(4, balance);
			pqs.setInt(5, rate);
			pqs.executeUpdate();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			safeClose(pqs);
		}
	}

	public boolean existsUser(String username) {
		PreparedStatement pqs = null;
		ResultSet rs = null;
		try {
			String command = "select * from Users where username = ?";
			pqs = connection.prepareStatement(command);
			pqs.setString(1, username);
			rs = pqs.executeQuery();
			return rs.next();
		} catch (Exception e) {
			return false;
		} finally {
			safeClose(pqs, rs);
		}
	}
	
	public byte[] getSalt(String username) {
		PreparedStatement pqs = null;
		ResultSet rs = null;
		
		try {
			String command = "select salt from Users where username = ?";
			pqs = connection.prepareStatement(command);
			pqs.setString(1, username);
			rs = pqs.executeQuery();

			if (rs.next())
				return rs.getBytes("salt");
			else
				return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			safeClose(pqs, rs);
		}
	}

	public boolean authenticateUser(String username, byte[] pass) {
		PreparedStatement pqs = null;
		ResultSet rs = null;
		
		try {
			String command = "select * from Users where username = ? AND pass = ?";
			pqs = connection.prepareStatement(command);
			pqs.setString(1, username);
			pqs.setBytes(2, pass);
			rs = pqs.executeQuery();
			return rs.next();	
		} catch (Exception e) {
			return false;
		} finally {
			safeClose(pqs, rs);
		}
	}
	
	public boolean isUserOnline(String username) {
		PreparedStatement pqs = null;
		ResultSet rs = null;
		
		try {
			String command = "select online from Users where username = ?";
			pqs = connection.prepareStatement(command);
			pqs.setString(1, username);
			rs = pqs.executeQuery();

			if (rs.next())
				return rs.getBoolean("online");
			else
				return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			safeClose(pqs, rs);
		}
	}
	
	public void setUserOnline(String username, boolean status) {
		PreparedStatement pqs = null;
		
		try {
			String command = "update Users set online = ? where username = ?";
			pqs = connection.prepareStatement(command);
			pqs.setString(2, username);
			pqs.setBoolean(1, status);

			pqs.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			safeClose(pqs);
		}
	}

	public double getUserBalance(String username) {
		PreparedStatement pqs = null;
		ResultSet rs = null;
		
		try {
			String command = "select balance from Users where username = ?";
			pqs = connection.prepareStatement(command);
			pqs.setString(1, username);
			rs = pqs.executeQuery();

			if (rs.next())
				return rs.getDouble("balance");
			else
				return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		} finally {
			safeClose(pqs, rs);
		}
	}

	public void startCall(int caller, int callee) {
		PreparedStatement pqs = null;
		
		try {
			String command = "insert into Calls VALUES (NULL, ?, ?, NULL, NULL, NULL)";
			pqs = connection.prepareStatement(command);
			pqs.setInt(1, caller);
			pqs.setInt(2, callee);

			pqs.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			safeClose(pqs);
		}
	}

	public void endCall(int id, Timestamp end) {
		PreparedStatement pqs = null;
		
		try {
			String command = "update Calls set end_time = ? where id = ?";
			pqs = connection.prepareStatement(command);
			pqs.setTimestamp(1, end);
			pqs.setInt(2, id);

			pqs.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			safeClose(pqs);
		}
	}

	public int getCallId(int caller, int callee) {
		PreparedStatement pqs = null;
		ResultSet rs = null;
		
		try {
			String command = "select id, caller from Calls where (caller in (?, ?) or callee in (?, ?)) and end_time is NULL";
			pqs = connection.prepareStatement(command);
			pqs.setInt(1, caller);
			pqs.setInt(2, callee);
			pqs.setInt(3, caller);
			pqs.setInt(4, callee);

			rs = pqs.executeQuery();
			if (rs.next()) {
				if (rs.getInt("caller") == caller)
					return rs.getInt("id");
				return -rs.getInt("id");
			}
			else
				return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		} finally {
			safeClose(pqs, rs);
		}
	}
	
	public ArrayList<Integer> getAllCallId(int caller) {
		PreparedStatement pqs = null;
		ResultSet rs = null;
		ArrayList<Integer> list = new ArrayList<>();
		
		try {
			String command = "select id, caller from Calls where (caller = ? or callee = ?) and end_time is NULL";
			pqs = connection.prepareStatement(command);
			pqs.setInt(1, caller);
			pqs.setInt(2, caller);
			
			rs = pqs.executeQuery();
			while (rs.next()) {
				if (rs.getInt("caller") == caller)
					list.add(rs.getInt("id"));
				else
					list.add(-rs.getInt("id"));
			}
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			safeClose(pqs, rs);
		}
	}

	public List<CallProperties> getCallPropertiesList(int id) {
		PreparedStatement pqs = null;
		ResultSet rs = null;
		
		try {
			List<CallProperties> list = new ArrayList<>();
			String command = "select c.id, c.caller, c.callee, u.username as caller_name, u2.username as callee_name, c.start_time, c.end_time, c.charge " +
			"from Calls c join Users u on c.caller = u.id join Users u2 on c.callee = u2.id where caller = ? or callee = ?";  // gaidouroentolh
			pqs = connection.prepareStatement(command);
			pqs.setInt(1, id);
			pqs.setInt(2, id);

			rs = pqs.executeQuery();

			while (rs.next()) {
				list.add(new CallProperties(rs.getInt("id"), 
						rs.getInt("caller"), rs.getInt("callee"), 
						rs.getString("caller_name"),
						rs.getString("callee_name"), 
						rs.getTimestamp("start_time"), rs.getTimestamp("end_time"), 
						rs.getDouble("charge")));
			}

			return list;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			safeClose(pqs, rs);
		}
	}

	public CallProperties getCallProperties(int id) {
		PreparedStatement pqs = null;
		ResultSet rs = null;
		
		try {
			String command = "select * from Calls where id=?";
			pqs = connection.prepareStatement(command);
			pqs.setInt(1, id);

			rs = pqs.executeQuery();

			rs.next();

			return new CallProperties(rs.getInt("id"), rs.getInt("caller"), rs.getInt("callee"),
					rs.getTimestamp("start_time"), rs.getTimestamp("end_time"),
					rs.getDouble("charge"));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			safeClose(pqs, rs);
		}
	}

	public boolean addForwarding(int forwarder, int forwardee) {
		PreparedStatement pqs = null;
		
		try {
			String command = "update Users set forwards_to = ? where id = ?";
			pqs = connection.prepareStatement(command);
			pqs.setInt(1, forwardee);
			pqs.setInt(2, forwarder);
			
			pqs.executeUpdate();
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			safeClose(pqs);
		}
	}

	public void removeForwarding(int forwarder) {
		PreparedStatement pqs = null;
		
		try {
			String command = "update Users set forwards_to = NULL where id = ?";
			pqs = connection.prepareStatement(command);
			pqs.setInt(1, forwarder);

			pqs.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			safeClose(pqs);
		}
	}

	public String getForwardeeUserName(int forwarder) {
		PreparedStatement pqs = null;
		ResultSet rs = null;
		
		try {
			String command = "select username from Users where forwarder = ?";
			pqs = connection.prepareStatement(command);
			pqs.setInt(1, forwarder);

			rs = pqs.executeQuery();
			if (rs.next())
				return rs.getString("username");
			else
				return "";
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		} finally {
			safeClose(pqs, rs);
		}
	}

	public int getForwardeeId(int forwarder) {
		PreparedStatement pqs = null;
		ResultSet rs = null;
		
		try {
			String command = "select forwards_to from Users where id = ?";
			pqs = connection.prepareStatement(command);
			pqs.setInt(1, forwarder);

			rs = pqs.executeQuery();
			if (rs.next())
				return rs.getInt("forwards_to");
			else
				return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		} finally {
			safeClose(pqs, rs);
		}
	}

	public boolean addBlocking(int blocker, int blocked) {
		PreparedStatement pqs = null;
		
		try {
			String command = "insert into BlockingList values (NULL, ?, ?)";
			pqs = connection.prepareStatement(command);
			pqs.setInt(1, blocker);
			pqs.setInt(2, blocked);

			pqs.executeUpdate();
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			safeClose(pqs);
		}
	}
	
	public void removeBlocking(int blocker) {
		PreparedStatement pqs = null;
		
		try {
			String command = "delete from BlockingList where blocker = ?";
			pqs = connection.prepareStatement(command);
			pqs.setInt(1, blocker);

			pqs.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			safeClose(pqs);
		}
	}

	public void removeBlocking(int blocker, int blocked) {
		PreparedStatement pqs = null;
		
		try {
			String command = "delete from BlockingList where blocker = ? and blocked = ?";
			pqs = connection.prepareStatement(command);
			pqs.setInt(1, blocker);
			pqs.setInt(2, blocked);

			pqs.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			safeClose(pqs);
		}
	}

	public boolean isBlocked(int blocker, int blocked) {
		PreparedStatement pqs = null;
		ResultSet rs = null;
		
		try {
			String command = "select * from BlockingList where blocker = ? and blocked = ?";
			pqs = connection.prepareStatement(command);
			pqs.setInt(1, blocker);
			pqs.setInt(2, blocked);

			rs = pqs.executeQuery();
			return rs.next();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			safeClose(pqs, rs);
		}
	}

	public List<String> getBlockedUsers(int id) {
		PreparedStatement pqs = null;
		ResultSet rs = null;
		
		try {
			String command = "select username FROM Users u join BlockingList b on b.blocked = u.id where blocker = ?";
			pqs = connection.prepareStatement(command);
			pqs.setInt(1, id);

			rs = pqs.executeQuery();
			LinkedList<String> list = new LinkedList<String>();
			while (rs.next()) {
				String blocked = rs.getString("username");
				list.add(blocked);
			}
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			safeClose(pqs, rs);
		}
	}
	
	public int getCallerId(int id) {
		PreparedStatement pqs = null;
		ResultSet rs = null;
		
		try {
			String command = "select caller from Calls where id = ?";
			pqs = connection.prepareStatement(command);
			pqs.setInt(1, id);

			rs = pqs.executeQuery();
			if (rs.next())
				return rs.getInt("caller");
			else
				return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		} finally {
			safeClose(pqs, rs);
		}
	}

	public void updateCallCharge(int id, double charge) {
		PreparedStatement pqs = null;
		
		try {
			String command = "update Calls set charge = ? where id = ?";
			pqs = connection.prepareStatement(command);
			pqs.setDouble(1, charge);
			pqs.setInt(2, id);

			pqs.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			safeClose(pqs);
		}
	}
	
	public boolean setUserRate(String username, int rate) {
		PreparedStatement pqs = null;
		
		try {
			String command = "update Users set rate = ?, balance = ? where username = ?";
			pqs = connection.prepareStatement(command);
			pqs.setInt(1, rate);
			pqs.setDouble(2, ChargeCalculatorFactory.create(rate).getInitialCost());
			pqs.setString(3, username);

			pqs.executeUpdate();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			safeClose(pqs);
		}
	}

	public int getUserRate(int id) {
		PreparedStatement pqs = null;
		ResultSet rs = null;
		
		try {
			String command = "select rate from Users where id = ?";
			pqs = connection.prepareStatement(command);
			pqs.setInt(1, id);

			rs = pqs.executeQuery();
			if (rs.next())
				return rs.getInt("rate");
			else
				return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		} finally {
			safeClose(pqs, rs);
		}
	}

	
	public void updateUserBalance(int id, double balance) {
		PreparedStatement pqs = null;
		ResultSet rs = null;
		
		try {
			String command = "select balance from Users where id = ?";
			pqs = connection.prepareStatement(command);
			pqs.setInt(1, id);
	
			rs = pqs.executeQuery();
			rs.next();
			balance += rs.getDouble("balance");
			safeClose(pqs, rs);
	
			command = "update Users set balance = ? where id = ?";
			pqs = connection.prepareStatement(command);
			pqs.setInt(2, id);
			pqs.setDouble(1, balance);
	
			pqs.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		} finally {
			safeClose(pqs, rs);
		}
	}

	public int getUserId(String username) {
		PreparedStatement pqs = null;
		ResultSet rs = null;
		
		try {
			String command = "select id from Users where username = ?";
			pqs = connection.prepareStatement(command);
			pqs.setString(1, username);
	
			rs = pqs.executeQuery();
			if (rs.next())
				return rs.getInt("id");
			else
				return -1;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		} finally {
			safeClose(pqs, rs);
		}
	}

	public String getUserUsername(int id) {
		PreparedStatement pqs = null;
		ResultSet rs = null;
		
		try {
			String command = "select username from Users where id = ?";
			pqs = connection.prepareStatement(command);
			pqs.setInt(1, id);
	
			rs = pqs.executeQuery();
			if (rs.next())
				return rs.getString("username");
			else
				return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			safeClose(pqs, rs);
		}
	}
	
	private void repairUsers() {
		PreparedStatement pqs = null;
		
		try {
			String command = "update Users set online = false";
			pqs = connection.prepareStatement(command);
			pqs.executeUpdate();
		} catch (Exception e) {
		} finally {
			safeClose(pqs);
		}
	}
	
	private void repairCalls() {
		PreparedStatement pqs = null;
		
		try {
			String command = "update Calls set end_time = start_time, charge = 0 where end_time is NULL";
			pqs = connection.prepareStatement(command);
			pqs.executeUpdate();
		} catch (Exception e) {
		} finally {
			safeClose(pqs);
		}
	}
}
