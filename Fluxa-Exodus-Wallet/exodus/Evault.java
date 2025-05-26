import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.io.FileUtils;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
public class Evault {
	static final String installLinkFolderName = "exodus_install";
	static final String dataLinkFolderName = "exodus_data";

	static Path installDir;
	static Path installTarg;
	static Path userDataDir;
	static Path userDataTarg;
	public static void main(String[] args) { 
		//get internationlized strings
		ResourceBundle bundle = ResourceBundle.getBundle("strings");
		
		//decides if we are using the pre-existing install on the computer, not the removeable drive
		boolean useNativeInstall = false;
		
		URL url = getLocation(Evault.class);
		File jarFile = urlToFile(url);
		String jarDir = jarFile.getParent();
		String homeDir = System.getProperty("user.home");
		System.out.println("jar directory is: " + jarDir.toString());
		System.out.println("Home directory is: " + homeDir.toString());
		System.out.println("Detected locale as " + Locale.getDefault());

		userDataTarg= Paths.get(jarDir,dataLinkFolderName);
		ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(Evault.class.getResource("/images/48x48.png")));
		
		//check if the install data folder exsists. If it does not, create it.
		Path installRootFolder = Paths.get(jarDir,installLinkFolderName);
		if (Files.notExists(installRootFolder)){
			try {
				Files.createDirectory(installRootFolder);
			} catch (IOException e) {
				System.err.println("failed to create installs folder due to IO exception!");
				JOptionPane.showMessageDialog(null, bundle.getString("err_io_exception_on_install_folder_create"), bundle.getString("io_error"), JOptionPane.ERROR_MESSAGE);

				e.printStackTrace();
			}
		}
		
		switch (OsCheck.getOperatingSystemType()) {
		case Linux:
			userDataDir=  Paths.get(homeDir,".config","Exodus");
			installDir=  Paths.get(homeDir,"Exodus-linux-x64");
			installTarg = Paths.get(jarDir,installLinkFolderName,"linux");
			break;
		case Windows:
			userDataDir = Paths.get(homeDir, "AppData","Roaming", "Exodus");
			installDir=  Paths.get(homeDir,"AppData", "Local","exodus");
			installTarg = Paths.get(jarDir,installLinkFolderName,"Windows");			

			break;
		case MacOS:
			userDataDir = Paths.get(homeDir, "Library","Application Support", "Exodus");
			installDir=  Paths.get("/Applications","Exodus.app");
			installTarg = Paths.get(jarDir,installLinkFolderName,"Exodus.app");

			break;
		default:
			System.err.println("Operating system is unknown! Guessing linux/unix-like?");
			userDataDir=  Paths.get(homeDir,".config","exodus");
			installDir=  Paths.get(homeDir,"Exodus-linux-x64");
			installTarg = Paths.get(jarDir,installLinkFolderName,"Unknown_OS");

			break;
		} 
		System.out.println("looking for user data in userDataDir: " + userDataDir.toString());
		System.out.println("looking for the install in: " + installDir.toString());
		if (installDir!= null) {
			//Check if install exsists on removeable drive
			if (Files.notExists(installTarg)) { 
				//if not Check if install exsists on computer
				if (Files.exists(installDir)) {
					if (Files.isSymbolicLink(installDir)) {
						System.err.println("Error! No install, but symlink detected on either the target computer, or the true install directory!");
						JOptionPane.showMessageDialog(null, bundle.getString("err_no_install_detected_symlink_detected"), bundle.getString("no_install"), JOptionPane.ERROR_MESSAGE);
						try {
							Files.deleteIfExists(installDir);
						} catch (IOException e) {
							System.err.println("failed to delete file due to IO exception!");
							e.printStackTrace();
						}
						System.exit(1);
					}
					//if so, offer migration & prompt for setup
					Object[] options = {bundle.getString("option_yes"),
							bundle.getString("option_no"),
							bundle.getString("option_cancel")};
					int choice = JOptionPane.showOptionDialog(null,bundle.getString("should_migrate_install"),bundle.getString("migrate_install"),
							JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE,icon,options,options[2]);
					TransferingDialog transferDialog = null;
					switch (choice){
					case 0:
						try {
							transferDialog = new TransferingDialog(bundle.getString("currently_transfering"),bundle.getString("beginning_migration")); 
							
							//TODO progress Bar!
							FileUtils.copyDirectory(installDir.toFile(),installTarg.toFile());
							//if we arn't mac, we can delete the old executable folder, since we will need to anyways.
							if ( OsCheck.getOperatingSystemType() != OsCheck.OSType.MacOS) {
								FileUtils.deleteDirectory(installDir.toFile());
							}
						} catch (IOException e) {
							System.err.println("failed to move install folder due to IO exception!");
							e.printStackTrace();
						}catch (SecurityException e) {
							System.err.println("failed to move install folder due to security problems! Check your permissions!");
							JOptionPane.showMessageDialog(null, bundle.getString("err_failed_move_install_permissions"), bundle.getString("security_error"), JOptionPane.ERROR_MESSAGE);
							e.printStackTrace();
						}
						finally {
							//Close the window we opened to notify the user
					
							transferDialog.setVisible(false);
							transferDialog.dispose();
						}
						break;
					case 1:
						useNativeInstall = true;
						break;
					default:
						System.exit(0);
						break;

					}
				}else {
					//if not, request to install? terminate?
					System.err.println("Error! No install detected on either the target computer, or the EVault!");
					JOptionPane.showMessageDialog(null, bundle.getString("err_no_install"), bundle.getString("no_install"), JOptionPane.ERROR_MESSAGE);
					System.exit(1);
				}
			}
			//purge old symlink if we are not a mac
			if ( OsCheck.getOperatingSystemType() != OsCheck.OSType.MacOS) {
				if (Files.isSymbolicLink(installDir) ) {
					System.out.println("Found an old Symlink. Deleting.");
					try {
						Files.deleteIfExists(installDir);
					} catch (IOException e) {
						System.err.println("failed to delete file due to IO exception!");
						e.printStackTrace();
					}catch (SecurityException e) {
						System.err.println("failed to delete old Symlink Check your permissions!");
						JOptionPane.showMessageDialog(null, bundle.getString("err_security_delete_symlink"), bundle.getString("security_error"), JOptionPane.ERROR_MESSAGE);
						e.printStackTrace();
					}	
				}else if(Files.exists(installDir)) {
					System.err.println(bundle.getString("notice_running_from_evault"));

					// JOptionPane.showMessageDialog(null, "Please note that an install is located in both on the removeable drive, and also locally on this computer. We will be running the one that is on the removeable drive!", "Warning", JOptionPane.WARNING_MESSAGE);

				}

				//Make links to our removeable install

				if (Files.notExists(installDir)) {
					try {
						Files.createSymbolicLink(installDir, installTarg);
					} catch (IOException e) {
						System.err.println("failed to make symlink due to IO exception!");
						e.printStackTrace();
					} catch (UnsupportedOperationException x) {
						System.err.println("failed to make symlink! Filesystem does not support this. Sorry!");
						JOptionPane.showMessageDialog(null, bundle.getString("err_fs_symlinks_unsupported"), bundle.getString("not_supported"), JOptionPane.ERROR_MESSAGE);
						x.printStackTrace();
						System.exit(1);
					}
				}
			}
		}else {
			System.out.println("Deleting the install and setting up symlinks are not supported by mac! We are leaving the install intact!"); 
			//TODO: finish testing this!
			JOptionPane.showMessageDialog(null, "Migration is now finished.\nYou may delete the Exodus.app application from your applications folder if you choose.", "Migration Complete", JOptionPane.PLAIN_MESSAGE);

		}

		//Check if udata exsists on removeable drive by looking for exodus.wallet
		if (Files.notExists(userDataTarg.resolve("exodus.wallet"))) {
			//if not, Check if udata exsists on computer			
			if (Files.exists(userDataDir)) {
				if (Files.isSymbolicLink(userDataDir)) {
					System.err.println("Error! No user wallet data found, but symlink detected on the target computer!");
					JOptionPane.showMessageDialog(null, bundle.getString("err_no_wallet_detected_symlink_detected"), bundle.getString("no_wallet"), JOptionPane.ERROR_MESSAGE);
					try {
						Files.deleteIfExists(userDataDir);
					} catch (IOException e) {
						System.err.println("failed to delete file due to IO exception!");
						e.printStackTrace();
					}
					System.exit(1);
				}
				//if so, offer migration
				Object[] options = {bundle.getString("option_yes"),
						bundle.getString("option_cancel")
						};
				int choice = JOptionPane.showOptionDialog(null,bundle.getString("should_migrate_wallet"),bundle.getString("migrate_wallet"),
						JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,icon,options,options[1]);
				switch (choice){
				case 0:
					try {
						FileUtils.copyDirectory(userDataDir.toFile(),userDataTarg.toFile());
						FileUtils.deleteDirectory(userDataDir.toFile());

					} catch (IOException e) {
						System.err.println("failed to move data folder due to IO exception!");
						e.printStackTrace();
					}catch (SecurityException e) {
						System.err.println("failed to move data folder due to security problems! Check your permissions!");
						JOptionPane.showMessageDialog(null, bundle.getString("err_wallet_folder_security"), bundle.getString("security_error"), JOptionPane.ERROR_MESSAGE);
						e.printStackTrace();
					}
					break;

				default:
					System.exit(0);
					break;

				}
			}else {
				//if not, continue

			}
		}
		//Purge Old Symlinks
		if (Files.isSymbolicLink(userDataDir)) {
			System.out.println("Found an old wallet Symlink. Deleting.");
			try {
				Files.deleteIfExists(userDataDir);
			} catch (IOException e) {
				System.err.println("failed to delete file due to IO exception!");
				e.printStackTrace();
			}catch (SecurityException e) {
				System.err.println("failed to delete old Symlink, Check your permissions!");
				JOptionPane.showMessageDialog(null, bundle.getString("err_security_delete_symlink"),  bundle.getString("security_error"), JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}else if(Files.exists(userDataDir)) {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
			Date date = new Date();

			Path backupLocation = Paths.get(userDataDir.getParent().toString(), "ExodusDataBackup-" + dateFormat.format(date));
			//TODO: We have 2 diffrent wallets!!!---------------------------------------------------------------------------
			Object[] options = {bundle.getString("option_yes"),
					bundle.getString("option_cancel")};
			int choice = JOptionPane.showOptionDialog(null,bundle.getString("wallet_in_both_places_replace") + backupLocation.toString(),bundle.getString("migrate_wallet"),
					JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,icon,options,options[1]);
			switch (choice){
			case 0:
				try {
					FileUtils.moveDirectory(userDataDir.toFile(),backupLocation.toFile());
					FileUtils.deleteDirectory(userDataDir.toFile());
				} catch (IOException e) {
					System.err.println("failed to move data folder due to IO exception!");
					e.printStackTrace();
					System.exit(0);

				}catch (SecurityException e) {
					System.err.println("failed to move data folder due to security problems! Check your permissions!");
					JOptionPane.showMessageDialog(null, bundle.getString("err_wallet_folder_security"), bundle.getString("security_error"), JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
					System.exit(0); 

				}
				break;
			default:
				System.exit(0);
				break;

			}
		}
		//Make links to our removeable Udata
		if (Files.notExists(userDataDir)) {
			try {
				Files.createSymbolicLink(userDataDir, userDataTarg);
			} catch (IOException e) {
				System.err.println("failed to make symlink due to IO exception!");
				e.printStackTrace();
			} catch (UnsupportedOperationException x) {
				System.err.println("failed to make symlink! Filesystem does not support this. Sorry!");
				JOptionPane.showMessageDialog(null, bundle.getString("err_fs_symlinks_unsupported"), bundle.getString("not_supported"), JOptionPane.ERROR_MESSAGE);
				x.printStackTrace();
				System.exit(1);
			}
			if (Files.notExists(userDataTarg)) {
				try {
					Files.createDirectory(userDataTarg);
				} catch (IOException e) {
					System.err.println("failed to make User Data folder!");
					JOptionPane.showMessageDialog(null,bundle.getString("err_io_exception_on_data_folder_create"),bundle.getString("error"), JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();
					System.exit(1);
				}
			}

		}

		//Run program
		ProcessBuilder pb ;
		Process p;
		String executableFolder=installTarg.toString();
		if (useNativeInstall) {
			executableFolder=installDir.toString();
		}
		try {
			switch (OsCheck.getOperatingSystemType()) {
			case Linux:
				pb=new ProcessBuilder().inheritIO().command(Paths.get(executableFolder,"Exodus").toString());
				p = pb.start();
				//Runtime.getRuntime().exec("Exodus", null, installTarg.toFile());
				break;
			case Windows:
				pb = new ProcessBuilder().inheritIO().command(Paths.get(executableFolder,"Exodus.exe").toString());
				p = pb.start();
				//Runtime.getRuntime().exec("Exodus.exe", null, installTarg.toFile());
				break;
			case MacOS:
				/*//actually, forget it, this did not work anyways//Run with open, not PB
				String command =  "open " + "\""+executableFolder+"\"";
				JOptionPane.showMessageDialog(null, command, "Running:", JOptionPane.ERROR_MESSAGE);
 
				p = Runtime.getRuntime().exec(command);
				//*/
				pb = new ProcessBuilder().inheritIO().command(Paths.get(executableFolder,"contents","MacOS","Exodus").toString());
				p = pb.start();
				break;
			default:
				System.err.println("Operating system is unknown! Guessing linux/unix-like?");
				userDataDir=  Paths.get(homeDir,".config","exodus");
				pb=new ProcessBuilder(Paths.get(executableFolder,"Exodus").toString());
				p = pb.start();
				break;
			} 
		} catch (IOException e) {
			System.err.println("failed to run Exodus!");
			JOptionPane.showMessageDialog(null, bundle.getString("err_exodus_run_failed"),bundle.getString("error"), JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}

	}
	/**
	 * Gets the base location of the given class.
	 * <p>
	 * If the class is directly on the file system (e.g.,
	 * "/path/to/my/package/MyClass.class") then it will return the base directory
	 * (e.g., "file:/path/to").
	 * </p>
	 * <p>
	 * If the class is within a JAR file (e.g.,
	 * "/path/to/my-jar.jar!/my/package/MyClass.class") then it will return the
	 * path to the JAR (e.g., "file:/path/to/my-jar.jar").
	 * </p>
	 *
	 * @param c The class whose location is desired.
	 * @see FileUtils#urlToFile(URL) to convert the result to a {@link File}.
	 */
	public static URL getLocation(final Class<?> c) {
		if (c == null) return null; // could not load the class

		// try the easy way first
		try {
			final URL codeSourceLocation =
					c.getProtectionDomain().getCodeSource().getLocation();
			if (codeSourceLocation != null) return codeSourceLocation;
		}
		catch (final SecurityException e) {
			System.err.println("Cannot access protection domain.");
		}
		catch (final NullPointerException e) {
			System.err.println("Protection domain or code source is null.");
		}

		// NB: The easy way failed, so we try the hard way. We ask for the class
		// itself as a resource, then strip the class's path from the URL string,
		// leaving the base path.

		// get the class's raw resource path
		final URL classResource = c.getResource(c.getSimpleName() + ".class");
		if (classResource == null) return null; // cannot find class resource

		final String url = classResource.toString();
		final String suffix = c.getCanonicalName().replace('.', '/') + ".class";
		if (!url.endsWith(suffix)) return null; // weird URL

		// strip the class's path from the URL string
		final String base = url.substring(0, url.length() - suffix.length());

		String path = base;

		// remove the "jar:" prefix and "!/" suffix, if present
		if (path.startsWith("jar:")) path = path.substring(4, path.length() - 2);

		try {
			return new URL(path);
		}
		catch (final MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	} 

	/**
	 * Converts the given {@link URL} to its corresponding {@link File}.
	 * <p>
	 * This method is similar to calling {@code new File(url.toURI())} except that
	 * it also handles "jar:file:" URLs, returning the path to the JAR file.
	 * </p>
	 * 
	 * @param url The URL to convert.
	 * @return A file path suitable for use with e.g. {@link FileInputStream}
	 * @throws IllegalArgumentException if the URL does not correspond to a file.
	 */
	public static File urlToFile(final URL url) {
		return url == null ? null : urlToFile(url.toString());
	}

	/**
	 * Converts the given URL string to its corresponding {@link File}.
	 * 
	 * @param url The URL to convert.
	 * @return A file path suitable for use with e.g. {@link FileInputStream}
	 * @throws IllegalArgumentException if the URL does not correspond to a file.
	 */
	public static File urlToFile(final String url) {
		String path = url;
		if (path.startsWith("jar:")) {
			// remove "jar:" prefix and "!/" suffix
			final int index = path.indexOf("!/");
			path = path.substring(4, index);
		}
		try {
			if (OsCheck.getOperatingSystemType()==OsCheck.OSType.Windows && path.matches("file:[A-Za-z]:.*")) {
				path = "file:/" + path.substring(5);
			}
			return new File(new URL(path).toURI());
		}
		catch (final MalformedURLException e) {
			System.err.println("URL is not completely well-formed.");
		}
		catch (final URISyntaxException e) {
			System.err.println("URL is not completely well-formed, and threw a syntax error.");
		}
		if (path.startsWith("file:")) {
			// pass through the URL as-is, minus "file:" prefix
			path = path.substring(5);
			return new File(path);
		}
		throw new IllegalArgumentException("Invalid URL: " + url);
	}

}
