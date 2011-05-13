import java.io.*;
import java.util.*;

import javax.microedition.io.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.rms.*;
import com.policyinnovations.expensetracker.*;

public class ExpenseTracker extends MIDlet implements CommandListener, Runnable {
	private Command mExitCommand, mNextCommand, mBackCommand, mLoginCommand,
			mSelectMenuCommand, mSaveCommand;
	private int mStep;
	private DateField mDate;
	private Form mForm, mLoginForm, mProgressForm;
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
	private List mBillTypeList;
	private List mProjectList;
	private TextField mAmount;
	private List mCityList;
	private List mCategoryList;
	private List mMainMenuList;

	private String expenseType, billType, project, amount, city, category,
			billID, details;

	public ExpenseTracker() {
		try {
			mPreferences = new Preferences("preferences");
		} catch (RecordStoreException rse) {
			mLoginForm = new Form("Exception");
			mLoginForm.append(new StringItem(null, rse.toString()));
			mLoginForm.setCommandListener(this);
			return;
		}
		// String[] stringMainMenu = { "Add New", "Upload Expenses",
		// "Update Lists", "Logout" };
		String[] stringMainMenu = { "Add New", "Upload Expenses",
				"Update Lists" };

		mMainMenuList = new List("Main Menu", List.IMPLICIT, stringMainMenu,
				null);

		String[] stringExpenseTypes = { "Personal", "Official" };
		String[] stringBillTypes = { "Billed", "Unbilled" };
		mExpenseTypeList = new List("Select Expense Type", List.IMPLICIT,
				stringExpenseTypes, null);
		mBillTypeList = new List("Select Bill Type", List.IMPLICIT,
				stringBillTypes, null);

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

				if (mPreferences.get(kBillDetailsList) != null) {
					if (mPreferences.get(kBillDetailsList).length() > 5) {
						lastBillNumber = new String(
								mPreferences.get(kLastBillNumber));
//						lastSelectedProject = new String(								mPreferences.get(kLastSelectedProject));
//						lastSelectedCity = new String(								mPreferences.get(kLastSelectedCity));

//						lastSelectedCategory = new String(								mPreferences.get(kLastSelectedCategory));
lastSelectedProject = new String("");
lastSelectedCity = new String("");
lastSelectedCategory = new String("");

						billDetailsList = new String(
								mPreferences.get(kBillDetailsList));
					}
				} else {
					lastBillNumber = "0";
					lastSelectedProject = "";
					lastSelectedCity = "";
					lastSelectedCategory = "";
					billDetailsList = "";
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
		mBackCommand = new Command("Back", Command.BACK, 0);
		mSaveCommand = new Command("Save", Command.SCREEN, 0);

		mMainMenuList.addCommand(mSelectMenuCommand);
		mMainMenuList.addCommand(mExitCommand);
		mMainMenuList.setCommandListener(this);
		mStep = 0;

		mExpenseTypeList.addCommand(mNextCommand);
		mExpenseTypeList.setCommandListener(this);

		if (mPreferences.get(kAuthToken) == null
				|| mPreferences.get(kAuthToken) == "") {
			authToken = "";
			mLoginForm = new Form("Login Form");
			mUsernameField = new TextField("Username", "", 100, TextField.ANY);
			mPasswordField = new TextField("Password", "", 100,
					TextField.PASSWORD);
			mLoginForm.append(mUsernameField);
			mLoginForm.append(mPasswordField);

			mLoginCommand = new Command("Login", Command.SCREEN, 0);
			mLoginForm.addCommand(mLoginCommand);
			mLoginForm.addCommand(mExitCommand);
			mLoginForm.setCommandListener(this);
		}
		mProgressForm = new Form("Login progress");
		mProgressForm.addCommand(mExitCommand);
		mProgressString = new StringItem(null, null);
		mProgressForm.append(mProgressString);
	}

	public void startApp() {
		System.out.println("\n*** Looks ok till here ***\n");
		if (mPreferences.get(kAuthToken) == null) {
			Display.getDisplay(this).setCurrent(mLoginForm);
		} else if (mPreferences.get(kAuthToken).length() < 10) {

		} else {
			/*
			 * if (lastSelectedProject != "") mProjectList.setSelectedIndex(
			 * getArrayIndex(lastSelectedProject, split(projectList, ",")),
			 * true); if (lastSelectedCity != "") mProjectList.setSelectedIndex(
			 * getArrayIndex(lastSelectedCity, split(cityList, ",")), true); if
			 * (lastSelectedCategory != "") mProjectList.setSelectedIndex(
			 * getArrayIndex(lastSelectedCategory, split(categoryList, ",")),
			 * true);
			 */
			Display.getDisplay(this).setCurrent(mMainMenuList);
		}
		// Display.getDisplay(this).setCurrent(mLoginForm);
	}

	public void pauseApp() {
	}

	public void destroyApp(boolean unconditional) {
		// Save the userID, username, authToken, projectList, cityList,
		// categoryList.
		saveData();
		notifyDestroyed();
	}

	public void commandAction(Command c, Displayable s) {
		if (c == mLoginCommand) {
			Display.getDisplay(this).setCurrent(mProgressForm);
			Thread t = new Thread(this);
			t.start();
		} else if (c == mSelectMenuCommand) {
			if (mMainMenuList.getSelectedIndex() == 0) {
				mStep = 0;
				showNextListPage(mExpenseTypeList);
				System.out.println("\n*** Expense type ***\n");
			} else if (mMainMenuList.getSelectedIndex() == 1) {
				Display.getDisplay(this).setCurrent(mProgressForm);
				Thread t = new Thread(this);
				t.start();
			} else if (mMainMenuList.getSelectedIndex() == 2) {
				Display.getDisplay(this).setCurrent(mProgressForm);
				Thread t = new Thread(this);
				t.start();
			} /*
			 * else if (mMainMenuList.getSelectedIndex() == 3) {
			 * 
			 * //logout(); //notifyDestroyed(); }
			 */
		} else if (c == mNextCommand) {
			displayScreen(mStep);
		} else if (c == mBackCommand) {
			/*
			 * if (mStep == 1) {
			 * Display.getDisplay(this).setCurrent(mMainMenuList); mStep = 0;
			 * return; } else if (mStep == 2) {
			 * Display.getDisplay(this).setCurrent(mExpenseTypeList); mStep = 1;
			 * return; } else if (mStep == 3) {
			 * Display.getDisplay(this).setCurrent(mBillTypeList); mStep = 2;
			 * return; } else if (mStep == 4) {
			 * Display.getDisplay(this).setCurrent(mProjectList); mStep = 3;
			 * return; } else if (mStep == 5) {
			 * Display.getDisplay(this).setCurrent(mCityList); mStep = 4;
			 * return; } else if (mStep == 6) {
			 * Display.getDisplay(this).setCurrent(mForm); mStep = 5; return; }
			 * else { Display.getDisplay(this).setCurrent(mCategoryList); mStep
			 * = 6; return; }
			 */
		} else if (c == mSaveCommand) {
			lastBillNumber = newBillNumber;
			String bds = new String(authToken + "," + city + "," + amount + ","
					+ expenseType + "," + project + "," + category + ","
					+ billID);
			if (billDetailsList != null && billDetailsList.length() > 5) {
				billDetailsList = billDetailsList + "|" + bds;
			} else {
				billDetailsList = bds;
			}
			System.out.println("\nBillDetailsList - " + billDetailsList + "\n");
			saveData();
			Display.getDisplay(this).setCurrent(mMainMenuList);
			saveData();
		} else if (c == mExitCommand) {
			saveData();
			notifyDestroyed();
		}
	}

	public void run() {
		if (mMainMenuList.getSelectedIndex() == 1) {
			try {
				uploadExpenses();
			} catch (IOException ioe) {
				Alert report = new Alert("Sorry",
						"Something went wrong and upload failed.", null, null);
				report.setTimeout(Alert.FOREVER);
				Display.getDisplay(this).setCurrent(report, mMainMenuList);
				return;
			}
			billDetailsList = "";
			saveData();
			Alert report = new Alert(
					"Success",
					"All expenses have been uploaded to web server successfully.",
					null, null);
			report.setTimeout(Alert.FOREVER);
			Display.getDisplay(this).setCurrent(mMainMenuList);
			return;
		}
		if (mMainMenuList.getSelectedIndex() == 2) {
			try {
				updateLists();
			} catch (IOException ioe) {
				Alert report = new Alert("Sorry",
						"Something went wrong and update failed.", null, null);
				report.setTimeout(Alert.FOREVER);
				Display.getDisplay(this).setCurrent(report, mMainMenuList);
				return;
			}
			Alert report = new Alert(
					"Success",
					"List of Projects, Cities and Categories have been updates successfully.",
					null, null);
			report.setTimeout(Alert.FOREVER);
			Display.getDisplay(this).setCurrent(mMainMenuList);
			return;
		}

		username = mUsernameField.getString();
		password = mPasswordField.getString();

		try {
			login();
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
		} catch (IOException ioe) {
			Alert report = new Alert("Sorry",
					"Something went wrong and login failed.", null, null);
			report.setTimeout(Alert.FOREVER);
			Display.getDisplay(this).setCurrent(report, mLoginForm);
			return;
		}
		mProjectList = new List("Select Project", List.IMPLICIT, split(
				projectList, ","), null);
		mCityList = new List("Select City", List.EXCLUSIVE,
				split(cityList, ","), null);
		mCategoryList = new List("Select Category", List.IMPLICIT, split(
				categoryList, ","), null);
		Display.getDisplay(this).setCurrent(mMainMenuList);
		mStep = 1;
	}

	private void login() throws IOException {
		HttpConnection hc = null;
		InputStream in = null;

		try {
			String baseURL = "http://xtrack.ep.io/mobile-login/";
			String url = baseURL + "?u=" + username + "&p=" + password;
			System.out.println(url);
			mProgressString.setText("Connecting...");
			hc = (HttpConnection) Connector.open(url);

			/*
			 * if (hc.getResponseCode() != HttpConnection.HTTP_OK) { Alert
			 * report = new Alert( "Sorry",
			 * "Something went wrong and login failed. Please try again.", null,
			 * null); report.setTimeout(Alert.FOREVER);
			 * Display.getDisplay(this).setCurrent(report, mLoginForm); return;
			 * }
			 */
			hc.setRequestProperty("Connection", "close");
			in = hc.openInputStream();

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

			// Clean up.
			in.close();
			hc.close();
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

	private void uploadExpenses() throws IOException {
		HttpConnection hc = null;
		InputStream in = null;

		mProgressForm = new Form("Uploading Data");
		mProgressString = new StringItem(null, null);

		try {
			String baseURL = "http://xtrack.ep.io/add-expense/";
			String url = baseURL + "?q=" + billDetailsList;
			System.out.println(url);
			mProgressString.setText("Connecting...");
			hc = (HttpConnection) Connector.open(url);

			/*
			 * if (hc.getResponseCode() != HttpConnection.HTTP_OK) { Alert
			 * report = new Alert( "Sorry",
			 * "Something went wrong and login failed. Please try again.", null,
			 * null); report.setTimeout(Alert.FOREVER);
			 * Display.getDisplay(this).setCurrent(report, mLoginForm); return;
			 * }
			 */
			hc.setRequestProperty("Connection", "close");
			in = hc.openInputStream();

			mProgressString.setText("Processing...");

			int length = 0;
			byte buffer[] = new byte[200]; // arbitrary buffer size
			String response = new String();
			while ((length = in.read(buffer)) != -1) {
				response += new String(buffer, 0, length);
			}

			lastBillNumber = response;

			// Clean up.
			in.close();
			hc.close();
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

	private void updateLists() throws IOException {
		HttpConnection hc = null;
		InputStream in = null;

		mProgressForm = new Form("Updating Lists");
		mProgressString = new StringItem(null, null);

		try {
			String baseURL = "http://xtrack.ep.io/sync/";
			String url = baseURL + "?token=" + authToken;
			System.out.println(url);
			mProgressString.setText("Connecting...");
			hc = (HttpConnection) Connector.open(url);

			/*
			 * if (hc.getResponseCode() != HttpConnection.HTTP_OK) { Alert
			 * report = new Alert( "Sorry",
			 * "Something went wrong and login failed. Please try again.", null,
			 * null); report.setTimeout(Alert.FOREVER);
			 * Display.getDisplay(this).setCurrent(report, mLoginForm); return;
			 * }
			 */
			hc.setRequestProperty("Connection", "close");
			in = hc.openInputStream();

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

			// Clean up.
			in.close();
			hc.close();
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
		mPreferences.put(kAuthToken, null);

		try {
			mPreferences.save();
		} catch (RecordStoreException rse) {
		}

		System.out.println("\n*** Looks ok till here ***\n");
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
				billType = "";
				project = "";
				lastSelectedProject = "";
				mStep = 3;
				showNextListPage(mCityList);
				break;
			}
			System.out.println("\n*** bill type  ***\n");
			showNextListPage(mBillTypeList);
			break;

		case 2:
			billType = mBillTypeList.getString(mBillTypeList.getSelectedIndex());
			showNextListPage(mProjectList);
			break;

		case 3:
			project = mProjectList.getString(mProjectList.getSelectedIndex());
			lastSelectedProject = project;
			showNextListPage(mCityList);
			break;
		case 4:
			city = mCityList.getString(mCityList.getSelectedIndex());
			lastSelectedCity = city;
			mForm = new Form("Bill Details");
			mForm.append(mAmount);
			mForm.addCommand(mNextCommand);
			mForm.setCommandListener(this);
			Display.getDisplay(this).setCurrent(mForm);
			mStep = 5;
			break;
		case 5:
			amount = mAmount.getString();
			showNextListPage(mCategoryList);
			break;
		case 6:
			category = mCategoryList.getString(mCategoryList.getSelectedIndex());
			lastSelectedCategory = category;
			mProgressForm = new Form("Bill Details");
			mProgressForm.addCommand(mSaveCommand);
			// mProgressForm.addCommand(mBackCommand);
			mProgressForm.setCommandListener(this);
			mProgressString = new StringItem(null, null);
			mProgressForm.append(mProgressString);
			mProgressString.setText("Creating Bill ID");
			Display.getDisplay(this).setCurrent(mProgressForm);

			System.out.println("\n*** preveious last bill number -> ***\n"
					+ lastBillNumber);
			if (lastBillNumber == null)
				lastBillNumber = "0";
			newBillNumber = Integer.toString((int) Integer
					.parseInt(lastBillNumber.trim() + 1));
			System.out.println("\n*** new last bill number -> ***\n"
					+ newBillNumber);
			billID = new String(
					userID
							+ split(projectIDList, ",")[mProjectList
									.getSelectedIndex()] + lastBillNumber);

			String result = new String("Bill ID - " + billID + "\nName - "
					+ username + "\nExpense - " + expenseType
					+ "\nBill Type - " + billType + "\nProject - " + project
					+ "\nAmount - " + amount + "\nCity - " + city);
			mProgressString.setText(result);
			System.out.println(result);
			break;
		}
	}

	private Form createNewForm() {
		mForm = new Form("Bill Details");
		mForm.addCommand(mNextCommand);
		mForm.addCommand(mBackCommand);
		mForm.setCommandListener(this);
		mStep += 1;
		return mForm;
	}

	private void showNextListPage(List l) {
		l.addCommand(mNextCommand);
		// l.addCommand(mBackCommand);
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
				System.out.println(result[loop]);
			}
		}
		return result;
	}

	public int getArrayIndex(String[] stringArray, String s) {
		int ArraySize = stringArray.length;// get the size of the array
		for (int i = 0; i < ArraySize; i++) {
			if (stringArray[i] == s) {
				return (i);
			}
		}
		return (-1);// didn't find
	}
}
