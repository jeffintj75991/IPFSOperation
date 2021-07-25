package com.ipfsops.processor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ipfsops.dao.IPFSOpsDAO;
import com.ipfsops.io.ipfs.api.IPFS;
import com.ipfsops.io.ipfs.api.MerkleNode;
import com.ipfsops.io.ipfs.api.NamedStreamable;

import io.ipfs.multihash.Multihash;

@Component

public class IPFSOpsProcessor {
	@Autowired
	private IPFSOpsDAO iPFSOpsDAO;

	private static final String INSERT_STMT = "INSERT INTO FILEHASHDTLS" + "( FILENAME,HASH,VERSION ) VALUES (?,?,?)";
	private static final String QUERY_STMT = "SELECT * FROM FILEHASHDTLS";
	private final String UPDATE_STMT ="UPDATE FILEHASHDTLS SET VERSION=? WHERE FILENAME=? ";
	public String writeIPFS(String payload) throws IOException, ClassNotFoundException, SQLException {

		String dburl = iPFSOpsDAO.getPropValues("dburl");
		String jdbcdrver = iPFSOpsDAO.getPropValues("jdbcdrver");
		String user = iPFSOpsDAO.getPropValues("user");
		String password = iPFSOpsDAO.getPropValues("password");
		String ipfsURL = iPFSOpsDAO.getPropValues("ipfsURL");

		ResultSet rs;
		String UpdateName = null;
		JSONObject jsonObjIPFS = new JSONObject(payload);
		MerkleNode response;
		String extension = null;

		IPFS ipfs = new IPFS(ipfsURL);
		String filePath = jsonObjIPFS.getString("filePath");
		File fileExistance = new File(filePath);
		if (!fileExistance.exists()) {
			return "File does not exist";
		}
		Path path = Paths.get(filePath);
		Path pathfileName = path.getFileName();
		Path pathName = path.getParent();
		String fileNameWithOutExt = pathfileName.toString().replaceFirst("[.][^.]+$", "");
		if (filePath.contains(".")) {
			extension = filePath.substring(filePath.lastIndexOf("."));
		}
		System.out.println("filePath:" + filePath);
		System.out.println("pathName from path:" + pathName.toString());
		System.out.println("FileName from path:" + pathfileName.toString());
		System.out.println("fileNameWithOutExt:" + fileNameWithOutExt);
		System.out.println("extension:" + extension);

		String fileName = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());
		if (fileName.equals("")) {
			return "Directories cannot be added to the IPFS system";
		}
		System.out.println("fileName:" + fileName);

		// System.out.println("ipfs url:" + ipfs);
		try {

			// System.out.println("Hash (base 58): " + response.hash.toBase58());

			Class.forName(jdbcdrver);
			Connection conn = DriverManager.getConnection(dburl, user, password);
			PreparedStatement insertStatement = conn.prepareStatement(INSERT_STMT);
			//String queryCheck = "SELECT VERSION,count(*) AS rowcount  from FILEHASHDTLS WHERE FILENAME = ? GROUP BY VERSION";
			//String queryCheck = "SELECT VERSION from FILEHASHDTLS WHERE FILENAME = ?";
			String queryCheck = "SELECT VERSION,count(*) AS rowcount  from FILEHASHDTLS WHERE FILENAME = ?";
			PreparedStatement insertCheck = conn.prepareStatement(queryCheck);
			insertCheck.setString(1, fileName);
			rs = insertCheck.executeQuery();

			NamedStreamable.FileWrapper file = new NamedStreamable.FileWrapper(new File(filePath));
			response = ipfs.add(file).get(0);

			if (rs.next()) {
				int count = rs.getInt("rowcount");
				System.out.println("count:" + count);

				if (count > 0) {
					String versionString = rs.getString("VERSION");
					int version=Integer.parseInt(versionString);
					UpdateName = fileNameWithOutExt + "." + (Integer.toString(version+1));
					
					//UPDATE VERSION
					PreparedStatement updateStatement=conn.prepareStatement(UPDATE_STMT);
					updateStatement.setString(1, Integer.toString(version+1));
	        		updateStatement.setString(2, fileName);   
	        		updateStatement.executeUpdate();
	        		
					String UpdateNameExtension = UpdateName + extension;
					
					String UpdatedfilePath = pathName.toString() + "\\" + UpdateNameExtension;
					System.out.println("UpdatePath:" + UpdatedfilePath);
					System.out.println("path:" + UpdatedfilePath);

					Path UpdatePath = Paths.get(UpdatedfilePath);

					Files.copy(path, UpdatePath);

					fileNameWithOutExt = UpdateName;

					NamedStreamable.FileWrapper fileWrap = new NamedStreamable.FileWrapper(new File(UpdatedfilePath));
					response = ipfs.add(fileWrap).get(0);
					insertStatement.setString(1, UpdateNameExtension);
					insertStatement.setString(2, response.hash.toBase58());
					insertStatement.setString(3, Integer.toString(version+1));
					insertStatement.executeUpdate();
					return "File:" + UpdateName + "  " + "Hash:" + response.hash.toBase58();
				} else {
					insertStatement.setString(1, fileName);
					insertStatement.setString(2, response.hash.toBase58());
					insertStatement.setString(3, "1");
					insertStatement.executeUpdate();
				}
			}

		} catch (IOException ex) {
			System.out.println(ex);
			return "Error whilst communicating with the IPFS node";
		}
		return "File:" + fileName + "  " + "Hash:" + response.hash.toBase58();
	}

	public String readIPFS(String payload) throws IOException, SQLException, ClassNotFoundException,RuntimeException {
		String dburl = iPFSOpsDAO.getPropValues("dburl");
		String jdbcdrver = iPFSOpsDAO.getPropValues("jdbcdrver");
		String user = iPFSOpsDAO.getPropValues("user");
		String password = iPFSOpsDAO.getPropValues("password");
		String ipfsURL = iPFSOpsDAO.getPropValues("ipfsURL");
		ResultSet rs;

		JSONObject jsonObjIPFS = new JSONObject(payload);

		String fileName = jsonObjIPFS.getString("fileName");
		IPFS ipfs = new IPFS(ipfsURL);
		
		String returnString = null;
		byte[] content = null;
		try {

			Class.forName(jdbcdrver);
			Connection conn = DriverManager.getConnection(dburl, user, password);
			PreparedStatement preparedStatement = conn.prepareStatement((QUERY_STMT + " WHERE FILENAME=?"));
			preparedStatement.setString(1, fileName);

			rs = preparedStatement.executeQuery();
			//System.out.println("rs:"+rs.getRow());
			if(rs.getRow()==0) {
				return fileName+" does not exist";
			}
			while (rs.next()) {
				String hash = rs.getString("HASH");
				Multihash multihash = Multihash.fromBase58(hash);
				content = ipfs.cat(multihash);
				returnString = new String(content);
				System.out.println("Content of " + hash + ": " + new String(content));
			}

			// Hash of a file

		} catch (IOException ex) {
			throw new RuntimeException("INPUT ERROR", ex);
			
		}catch (RuntimeException ex) {
			throw new RuntimeException("CONNECTION ISSUE", ex);
		}

		return returnString;
	}

	public String listIPFS() throws IOException, ClassNotFoundException, SQLException {
		String dburl = iPFSOpsDAO.getPropValues("dburl");
		String jdbcdrver = iPFSOpsDAO.getPropValues("jdbcdrver");
		String user = iPFSOpsDAO.getPropValues("user");
		String password = iPFSOpsDAO.getPropValues("password");
		ResultSet rs;
		JSONObject outputJson = new JSONObject();

		Class.forName(jdbcdrver);
		Connection conn = DriverManager.getConnection(dburl, user, password);
		PreparedStatement preparedStatement = conn.prepareStatement((QUERY_STMT));

		rs = preparedStatement.executeQuery();
		while (rs.next()) {
			String FileName = rs.getString("FILENAME");
			String hash = rs.getString("HASH");
			outputJson.put(FileName, hash);
			// outputJson.put("Hash of the file", hash);
		}

		return outputJson.toString();
	}

}
