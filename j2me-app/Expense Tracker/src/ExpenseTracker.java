import java.io.*;
import java.util.*;

import javax.microedition.io.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.rms.*;

import com.expensetracker.*;

public class ExpenseTracker extends MIDlet implements CommandListener, Runnable {
	private static final String domainName = "http://xtrack.ep.io"; // Should
																	// not end
																	// with
																	// slash.
	private Command mExitCommand, mNextCommand, mBackCommand, mLoginCommand,
			mCancelLoginCommand, mSelectMenuCommand, mSaveCommand;
	private int mStep;
	private Form mAmountForm, mLoginForm, mProgressForm;
	private StringItem mProgressString;

	private static final String kUserID = "userID";
	private static final String kProjectIDList = "projectIDList";
	private static final String kUsername = "username";
	private static final String kAuthToken = "authToken";
	private static final String kTokenID = "tokenID";
	private static final String kProjectList = "projectList";
	private static final String kCityList = "cityList";
	private static final String kCategoryList = "categoryList";
	private static final String kLastBillNumber = "lastBillNumber";
	private static final String kLastSelectedProject = "lastSelectedProject";
	private static final String kLastSelectedCity = "lastSelectedCity";
	private static final String kLastSelectedCategory = "lastSelectedCategory";
	private static final String kBillDetailsList = "billDetailsList";

	private String userID, projectIDList, username, password, authToken,
			tokenID, projectList, cityList, categoryList, lastBillNumber,
			newBillNumber, lastSelectedProject, lastSelectedCity,
			lastSelectedCategory, billDetailsList;

	private Preferences mPreferences;

	private TextField mUsernameField, mPasswordField;
	private List mExpenseTypeList;
	private List mProjectList;
	private TextField mAmount;
	private List mCityList;
	private List mCategoryList;
	private List mMainMenuList;

	private String expenseType, project, amount, city, category, billID,
			details;

	public ExpenseTracker() {
		try {
			mPreferences = new Preferences("preferences");
		} catch (RecordStoreException rse) {
			mLoginForm = new Form("Exception");
			mLoginForm.append(new StringItem(null, rse.toString()));
			mLoginForm.setCommandListener(this);
			return;
		}
		String[] stringMainMenu = { "Add New", "Upload Expenses",
				"Update Lists", "Logout" };

		mMainMenuList = new List("Main Menu", List.IMPLICIT, stringMainMenu,
				null);

		String[] stringExpenseTypes = { "Personal", "Official Billed",
				"Official Unbilled" };
		mExpenseTypeList = new List("Select Expense Type", List.IMPLICIT,
				stringExpenseTypes, null);

		String[] stringProjects = { "Project 1", "Project 2" };
		String[] stringCities = { "Delhi", "hyderabad" };
		String[] stringCategories = { "Trvel", "Food" };

		if (mPreferences.get(kAuthToken) != null) {
			if (mPreferences.get(kAuthToken).length() > 5) {
				userID = new String(mPreferences.get(kUserID));
				projectIDList = new String(mPreferences.get(kProjectIDList));
				username = new String(mPreferences.get(kUsername));
				authToken = new String(mPreferences.get(kAuthToken));
				tokenID = new String(mPreferences.get(kTokenID));
				projectList = new String(mPreferences.get(kProjectList));
				cityList = new String(mPreferences.get(kCityList));
				categoryList = new String(mPreferences.get(kCategoryList));

				if (mPreferences.get(kLastSelectedCity) != null
						|| mPreferences.get(kLastSelectedCity) != "") {
					lastSelectedCity = new String(
							mPreferences.get(kLastSelectedCity));
					lastSelectedCategory = new String(
							mPreferences.get(kLastSelectedCategory));
				} else {
					lastSelectedCity = "";
					lastSelectedCategory = "";
				}
				
				if (mPreferences.get(kLastSelectedProject) != null
						|| mPreferences.get(kLastSelectedProject) != "") {
					lastSelectedProject = new String(
							mPreferences.get(kLastSelectedProject));
				}
				else {
					lastSelectedProject = "";
				}
				
				if (mPreferences.get(kBillDetailsList) != null
						|| mPreferences.get(kBillDetailsList) != "") {
					billDetailsList = new String(mPreferences.get(kBillDetailsList));
				}
				else {
					billDetailsList = "";
				}
				
				if (mPreferences.get(kLastBillNumber) != null
						|| mPreferences.get(kLastBillNumber) != "") {
					lastBillNumber = new String(mPreferences.get(kLastBillNumber));
				}
				else {
					lastBillNumber = "";
				}

				stringProjects = split(projectList, ",");
				stringCities = split(cityList, ",");
				stringCategories = split(categoryList, ",");
			}
		}

		mProjectList = new List("Select Project", List.IMPLICIT,
				stringProjects, null);
		mAmount = new TextField("Amount", "", 40, TextField.NUMERIC);
		mCityList = new List("Select City", List.IMPLICIT, stringCities, null);
		mCategoryList = new List("Select Category", List.IMPLICIT,
				stringCategories, null);

		mSelectMenuCommand = new Command("Select", Command.SCREEN, 0);
		mNextCommand = new Command("Next", Command.SCREEN, 0);
		mExitCommand = new Command("Exit", Command.EXIT, 0);
		mCancelLoginCommand = new Command("Cancel", Command.SCREEN, 0);
		mBackCommand = new Command("Back", Command.BACK, 0);
		mSaveCommand = new Command("Save", Command.SCREEN, 0);

		mMainMenuList.addCommand(mSelectMenuCommand);
		mMainMenuList.addCommand(mExitCommand);
		mMainMenuList.setCommandListener(this);
		mStep = 0;

		mExpenseTypeList.addCommand(mNextCommand);
		mExpenseTypeList.addCommand(mBackCommand);
		mExpenseTypeList.setCommandListener(this);

		mLoginForm = new Form("Login Form");
		mUsernameField = new TextField("Username", "", 100, TextField.ANY);
		mPasswordField = new TextField("Password", "", 100, TextField.PASSWORD);
		mLoginForm.append(mUsernameField);
		mLoginForm.append(mPasswordField);
		mLoginCommand = new Command("Login", Command.SCREEN, 0);
		mLoginForm.addCommand(mLoginCommand);
		mLoginForm.addCommand(mExitCommand);
		mLoginForm.setCommandListener(this);

		mProgressForm = new Form("Login progress");
		mProgressForm.addCommand(mCancelLoginCommand);
		mProgressString = new StringItem(null, null);
		mProgressForm.append(mProgressString);

		mAmountForm = new Form("Bill Details");
		mAmountForm.append(mAmount);
		mAmountForm.addCommand(mNextCommand);
		mAmountForm.addCommand(mBackCommand);
		mAmountForm.setCommandListener(this);
	}

	public void startApp() {
		if (mPreferences.get(kAuthToken) == null) {
			Display.getDisplay(this).setCurrent(mLoginForm);
		} else if (mPreferences.get(kAuthToken).length() < 10) {
			Display.getDisplay(this).setCurrent(mLoginForm);
		} else {
			if (lastSelectedProject != "") {
				int selProjectIndex = getArrayIndex(split(projectList, ","),
						lastSelectedProject);
				if (selProjectIndex != -1) {
					mProjectList.setSelectedIndex(selProjectIndex, true);
				}
			}
			if (lastSelectedCity != "") {
				int selCityIndex = getArrayIndex(split(cityList, ","),
						lastSelectedCity);
				int selCategoryIndex = getArrayIndex(split(categoryList, ","),
						lastSelectedCategory);
				if (selCityIndex != -1) {
					mCityList.setSelectedIndex(selCityIndex, true);
				}
				if (selCategoryIndex != -1) {
					mCategoryList.setSelectedIndex(selCategoryIndex, true);
				}
			}
			Display.getDisplay(this).setCurrent(mMainMenuList);
		}
	}

	public void pauseApp() {
	}

	public void destroyApp(boolean unconditional) {
		saveData();
		notifyDestroyed();
	}

	public void commandAction(Command c, Displayable s) {
		if (c == mLoginCommand) {
			Display.getDisplay(this).setCurrent(mProgressForm);
			Thread t = new Thread(this);
			t.start();
		} else if (c == mCancelLoginCommand) {
			Display.getDisplay(this).setCurrent(mLoginForm);
		} else if (c == mSelectMenuCommand) {
			if (mMainMenuList.getSelectedIndex() == 0) {
				mStep = 0;
				showNextListPage(mExpenseTypeList);
			} else if (mMainMenuList.getSelectedIndex() == 1) {
				if (billDetailsList == null || billDetailsList == "") {
					Alert report = new Alert("Uploading", "Nothing to upload.",
							null, null);
					report.setTimeout(Alert.FOREVER);
					Display.getDisplay(this).setCurrent(report, mMainMenuList);
					return;
				}
				mProgressForm.removeCommand(mBackCommand);
				mProgressForm.removeCommand(mSaveCommand);
				mProgressForm.removeCommand(mCancelLoginCommand);
				mProgressString
						.setText("Uploading expenses stored locally in your device.");
				Display.getDisplay(this).setCurrent(mProgressForm);
				Thread t = new Thread(this);
				t.start();
			} else if (mMainMenuList.getSelectedIndex() == 2) {
				mProgressForm.removeCommand(mBackCommand);
				mProgressForm.removeCommand(mSaveCommand);
				mProgressForm.removeCommand(mCancelLoginCommand);
				mProgressString
						.setText("Updating list of Projects, Cities and Categories.");
				Display.getDisplay(this).setCurrent(mProgressForm);
				Thread t = new Thread(this);
				t.start();
			} else if (mMainMenuList.getSelectedIndex() == 3) {
				saveData();
				if (billDetailsList == null) {
					billDetailsList = "";
				}
				if (billDetailsList.length() > 0) {
					Alert report = new Alert("Upload Local Data",
							"Please upload your expenses before logging out.",
							null, null);
					report.setTimeout(Alert.FOREVER);
					Display.getDisplay(this).setCurrent(report, mMainMenuList);
					return;
				}
				logout();
				Display.getDisplay(this).setCurrent(mLoginForm);
			}
		} else if (c == mNextCommand) {
			displayScreen(mStep);
		} else if (c == mBackCommand) {
			if (mStep == 1) {
				Display.getDisplay(this).setCurrent(mMainMenuList);
				mStep = 0;
				return;
			} else if (mStep == 2) {
				Display.getDisplay(this).setCurrent(mExpenseTypeList);
				mStep = 1;
				return;
			} else if (mStep == 3) {
				if (expenseType == "Personal") {
					Display.getDisplay(this).setCurrent(mExpenseTypeList);
					mStep = 1;
					return;
				}
				Display.getDisplay(this).setCurrent(mProjectList);
				mStep = 2;
				return;
			} else if (mStep == 4) {
				Display.getDisplay(this).setCurrent(mCityList);
				mStep = 3;
				return;
			} else if (mStep == 5) {
				Display.getDisplay(this).setCurrent(mAmountForm);
				mStep = 4;
				return;
			} else {
				Display.getDisplay(this).setCurrent(mCategoryList);
				mStep = 5;
				return;
			}

		} else if (c == mSaveCommand) {
			lastBillNumber = newBillNumber;
			String bds;
			if (expenseType.equals("Personal")) {
			} else {
				expenseType = "Official";
			}

			String timeStamp = Long
					.toString((long) System.currentTimeMillis() / 1000);

			bds = new String(authToken + "," + city + "," + amount + ","
					+ expenseType + "," + project + "," + category + ","
					+ billID + "," + timeStamp);

			if (billDetailsList != null && billDetailsList.length() > 5) {
				billDetailsList = billDetailsList + "|" + bds;
			} else {
				billDetailsList = bds;
			}
			System.out.println("\nBillDetailsList - " + billDetailsList + "\n");
			saveData();
			Display.getDisplay(this).setCurrent(mMainMenuList);
		} else if (c == mExitCommand) {
			saveData();
			notifyDestroyed();
		}
	}

	public void run() {
		if (mMainMenuList.getSelectedIndex() == 1) {
			try {
				int response_code = uploadExpenses();
				if (response_code == 200) {
					billDetailsList = "";
					saveData();
					Alert report = new Alert(
							"Success",
							"All expenses have been uploaded to web server successfully.",
							null, null);
					report.setTimeout(Alert.FOREVER);
					Display.getDisplay(this).setCurrent(mMainMenuList);
					return;
				} else {
					Alert report = new Alert("Sorry",
							"Something went wrong and upload failed.", null,
							null);
					report.setTimeout(Alert.FOREVER);
					Display.getDisplay(this).setCurrent(report, mMainMenuList);
					return;
				}
			} catch (IOException ioe) {
				return;
			}
		} else if (mMainMenuList.getSelectedIndex() == 2) {
			try {
				int response_code = updateLists();
				if (response_code == 200) {
					Alert report = new Alert(
							"Success",
							"List of Projects, Cities and Categories have been updated successfully.",
							null, null);
					report.setTimeout(Alert.FOREVER);
					Display.getDisplay(this).setCurrent(mMainMenuList);
					return;
				} else {
					Alert report = new Alert("Sorry",
							"Something went wrong and update failed.", null,
							null);
					report.setTimeout(Alert.FOREVER);
					Display.getDisplay(this).setCurrent(report, mMainMenuList);
					return;
				}
			} catch (IOException ioe) {
				return;
			}
		} else {
			username = mUsernameField.getString();
			password = mPasswordField.getString();
			try {
				int response_code = login();
				if (response_code == 200) {
					mPreferences.put(kUserID, userID);
					mPreferences.put(kProjectIDList, projectIDList);
					mPreferences.put(kUsername, username);
					mPreferences.put(kAuthToken, authToken);
					mPreferences.put(kTokenID, tokenID);
					mPreferences.put(kProjectList, projectList);
					mPreferences.put(kCityList, cityList);
					mPreferences.put(kCategoryList, categoryList);
					try {
						mPreferences.save();
					} catch (RecordStoreException rse) {
					}
				} else if (response_code == 404) {
					Alert report = new Alert(
							"Sorry",
							"Username and Password didn't match. Please try again.",
							null, null);
					report.setTimeout(Alert.FOREVER);
					Display.getDisplay(this).setCurrent(report, mLoginForm);
				} else {
					Alert report = new Alert("Sorry",
							"Something went wrong and login failed.", null,
							null);
					report.setTimeout(Alert.FOREVER);
					Display.getDisplay(this).setCurrent(report, mLoginForm);
				}
			} catch (IOException ioe) {
				return;
			}
			mProjectList = new List("Select Project", List.IMPLICIT, split(
					projectList, ","), null);
			mCityList = new List("Select City", List.EXCLUSIVE, split(cityList,
					","), null);
			mCategoryList = new List("Select Category", List.IMPLICIT, split(
					categoryList, ","), null);
			Display.getDisplay(this).setCurrent(mMainMenuList);
			mStep = 0;
		}
	}

	private int login() throws IOException {
		HttpConnection hc = null;
		InputStream in = null;
		int response_code = 0;
		try {
			String baseURL = domainName + "/mobile-login/";
			String url = baseURL + "?u=" + URLEncoder.encode(username, "UTF-8")
					+ "&p=" + URLEncoder.encode(password, "UTF-8");
			System.out.println(url);
			mProgressString.setText("Connecting...");
			hc = (HttpConnection) Connector.open(url);

			response_code = hc.getResponseCode();
			System.out
					.println("\nHTTP Response Code - " + response_code + "\n");
			// System.out.println("\n***Reached here ***\n");
			in = hc.openInputStream();
			// hc.setRequestProperty("Connection", "close");
			if (response_code != 200) {
				in.close();
				hc.close();
				System.out.println("\n***Could not login***\n");
				return response_code;
			} else {
				mProgressString.setText("Reading...");
				int length = 0;
				byte buffer[] = new byte[200]; // arbitrary buffer size
				details = new String();
				while ((length = in.read(buffer)) != -1) {
					details += new String(buffer, 0, length);
				}

				System.out.println(details);
				String[] temp = split(details, "|");

				userID = temp[0];
				authToken = temp[1];
				tokenID = temp[2];
				projectList = temp[3];
				projectIDList = temp[4];
				categoryList = temp[5];
				cityList = temp[6];
				lastBillNumber = temp[7];

				in.close();
				hc.close();

				return response_code;
			}
		} finally {
			try {
				if (in != null)
					in.close();
				if (hc != null)
					hc.close();
			} catch (IOException ignored) {
			}
		}
	}

	private int uploadExpenses() throws IOException {
		HttpConnection hc = null;
		InputStream in = null;
		int response_code = 0;

		mProgressForm = new Form("Uploading Data");
		mProgressString = new StringItem(null, null);

		try {
			String baseURL = domainName + "/add-expense/";
			String url = baseURL + "?q="
					+ URLEncoder.encode(billDetailsList, "UTF-8");
			System.out.println(url);

			mProgressString.setText("Connecting...");
			hc = (HttpConnection) Connector.open(url);

			response_code = hc.getResponseCode();
			System.out
					.println("\nHTTP Response Code - " + response_code + "\n");

			// hc.setRequestProperty("Connection", "close");
			in = hc.openInputStream();
			if (response_code != 200) {
				in.close();
				hc.close();
				System.out.println("\n***Could not login***\n");
				return response_code;
			} else {
				mProgressString.setText("Processing...");

				int length = 0;
				byte buffer[] = new byte[200]; // arbitrary buffer size
				String response = new String();
				while ((length = in.read(buffer)) != -1) {
					response += new String(buffer, 0, length);
				}

				lastBillNumber = response;
				saveData();
				in.close();
				hc.close();
				return response_code;
			}

		} finally {
			try {
				if (in != null)
					in.close();
				if (hc != null)
					hc.close();
			} catch (IOException ignored) {
			}
		}
	}

	private int updateLists() throws IOException {
		HttpConnection hc = null;
		InputStream in = null;
		int response_code = 0;

		mProgressForm = new Form("Updating Lists");
		mProgressString = new StringItem(null, null);

		try {
			String baseURL = domainName + "/sync/";
			String url = baseURL + "?token=" + authToken;
			System.out.println(url);
			mProgressString.setText("Connecting...");
			hc = (HttpConnection) Connector.open(url);
			
			response_code = hc.getResponseCode();
			System.out
			.println("\nHTTP Response Code - " + response_code + "\n");
			
			// hc.setRequestProperty("Connection", "close");
			in = hc.openInputStream();
			if (response_code != 200) {
				in.close();
				hc.close();
				System.out.println("\n***Could not login***\n");
				return response_code;
			} else {
				mProgressString.setText("Processing...");
				int length = 0;
				byte buffer[] = new byte[200]; // arbitrary buffer size
				details = new String();
				while ((length = in.read(buffer)) != -1) {
					details += new String(buffer, 0, length);
				}

				System.out.println(details);
				String[] temp = split(details, "|");
				userID = temp[0];
				authToken = temp[1];
				tokenID = temp[2];
				projectList = temp[3];
				projectIDList = temp[4];
				categoryList = temp[5];
				cityList = temp[6];
				lastBillNumber = temp[7];

				saveData();

				in.close();
				hc.close();
				return response_code;
			}
		} finally {
			try {
				if (in != null)
					in.close();
				if (hc != null)
					hc.close();
			} catch (IOException ignored) {
			}
		}
	}

	private void logout() {
		authToken = "";
		// System.out.println("\n*** About to logout. ***\n");
		mPreferences.put(kAuthToken, authToken);
		try {
			mPreferences.save();
		} catch (RecordStoreException rse) {
		}
		// System.out.println("\n*** Logged out Successfully. ***\n");
	}

	private void saveData() {
		mPreferences.put(kUserID, userID);
		mPreferences.put(kProjectIDList, projectIDList);
		mPreferences.put(kUsername, username);
		mPreferences.put(kAuthToken, authToken);
		mPreferences.put(kTokenID, tokenID);

		mPreferences.put(kProjectList, projectList);
		mPreferences.put(kCityList, cityList);
		mPreferences.put(kCategoryList, categoryList);

		mPreferences.put(kLastSelectedProject, lastSelectedProject);
		mPreferences.put(kLastSelectedCity, lastSelectedCity);
		mPreferences.put(kLastSelectedCategory, lastSelectedCategory);
		mPreferences.put(kLastBillNumber, lastBillNumber);
		mPreferences.put(kBillDetailsList, billDetailsList);

		try {
			mPreferences.save();
		} catch (RecordStoreException rse) {
		}
	}

	private void displayScreen(int step) {
		switch (mStep) {
		case 1:
			expenseType = mExpenseTypeList.getString(mExpenseTypeList
					.getSelectedIndex());
			System.out.println("\n*** expense type sleected ***\n");
			if (expenseType == "Personal") {
				project = "";
				lastSelectedProject = "";
				mStep = 2;
				showNextListPage(mCityList);
				break;
			}
			showNextListPage(mProjectList);
			break;
		case 2:
			project = mProjectList.getString(mProjectList.getSelectedIndex());
			lastSelectedProject = project;
			saveData();
			showNextListPage(mCityList);
			break;
		case 3:
			city = mCityList.getString(mCityList.getSelectedIndex());
			lastSelectedCity = city;
			saveData();
			Display.getDisplay(this).setCurrent(mAmountForm);
			mStep = 4;
			break;
		case 4:
			amount = mAmount.getString();
			showNextListPage(mCategoryList);
			break;
		case 5:
			category = mCategoryList
					.getString(mCategoryList.getSelectedIndex());
			lastSelectedCategory = category;
			saveData();
			mProgressForm = new Form("Bill Details");
			mProgressForm.addCommand(mSaveCommand);
			mProgressForm.addCommand(mBackCommand);
			mProgressForm.setCommandListener(this);
			mProgressString = new StringItem(null, null);
			mProgressForm.append(mProgressString);
			mProgressString.setText("Creating Bill ID");
			Display.getDisplay(this).setCurrent(mProgressForm);

			billID = "";
			if (expenseType == "Official Billed") {
				System.out.println("\n*** preveious last bill number -> ***\n"
						+ lastBillNumber);
				int num = (int) Integer.parseInt(lastBillNumber.trim()) + 1;
				newBillNumber = Integer.toString(num);
				// System.out.println("\n*** new last bill number -> ***\n" +
				// newBillNumber);
				billID = new String(
						userID
								+ split(projectIDList, ",")[mProjectList
										.getSelectedIndex()] + tokenID
								+ newBillNumber);
			}
			String result = new String("Bill ID - " + billID + "\nName - "
					+ username + "\nExpense - " + expenseType + "\nProject - "
					+ project + "\nAmount - " + amount + "\nCategory - "
					+ category + "\nCity - " + city);
			mProgressString.setText(result);
			// System.out.println(result);
			break;
		}
	}

	private void showNextListPage(List l) {
		l.addCommand(mNextCommand);
		l.addCommand(mBackCommand);
		l.setCommandListener(this);
		Display.getDisplay(this).setCurrent(l);
		mStep += 1;
	}

	private String[] split(String original, String separator) {
		Vector nodes = new Vector();
		// Parse nodes into vector
		int index = original.indexOf(separator);
		while (index >= 0) {
			nodes.addElement(original.substring(0, index));
			original = original.substring(index + separator.length());
			index = original.indexOf(separator);
		}
		// Get the last node
		nodes.addElement(original);

		// Create split string array
		String[] result = new String[nodes.size()];
		if (nodes.size() > 0) {
			for (int loop = 0; loop < nodes.size(); loop++) {
				result[loop] = (String) nodes.elementAt(loop);
				// System.out.println(result[loop]);
			}
		}
		return result;
	}

	public int getArrayIndex(String[] stringArray, String s) {
		int ArraySize = stringArray.length;// get the size of the array
		for (int i = 0; i < ArraySize; i++) {
			if (stringArray[i].equals(s)) {
				return (i);
			}
		}
		return (-1);// could't find
	}
}
