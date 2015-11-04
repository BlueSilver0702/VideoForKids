/* Copyright (c) 2012-2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wglxy.example.trivialdrive2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.wglxy.example.trivialdrive2.R;
import com.wglxy.example.trivialdrive2.util.IabHelper;
import com.wglxy.example.trivialdrive2.util.IabResult;
import com.wglxy.example.trivialdrive2.util.Inventory;
import com.wglxy.example.trivialdrive2.util.Purchase;

import java.util.ArrayList;

import android.content.res.Resources;
import static com.wglxy.example.trivialdrive2.Constants.*;

/**
 * This class is derived from the main activity in the TrivialDrive example
 * from the Google Developers website.
 *
 * It adds the code necessary to support in-app billing. A subclass that wants
 * that code to run should arrange to call the IAB method setupIabHelper.
 * The subclass should also override methods onIabSetupFailed and onIabSetupSucceeded.
 */

public abstract class IabActivity extends Activity {

	public IabActivity() {
      super ();
		// TODO Auto-generated constructor stub
	}

	/*
	public IabActivity(int requestedClients) {
		super(requestedClients);
		// TODO Auto-generated constructor stub
	}
	*/

/**
 */
// Constants and variables

final String LOG_TAG = Constants.LOG_IAB;

static final int RC_PURCHASE_REQUEST = Constants.RC_PURCHASE_REQUEST;

boolean mDestroyed = false;

/**
 */
// Variables that track purchase of in-app items.
// These are here so that each subclass does not have to do the same thing.
// (If there is only one subclass of IabActivity, or if there are different sets of items being
// managed in different activities, you might not have any shared variables here.)

// Does the user have the premium upgrade?
protected boolean mIsPremium = false;

// Does the user have an active subscription to the infinite gas plan?
protected boolean mSubscribedToInfiniteGas = false;

// Current amount of gas in tank, in units
// protected int mTank;

/**
 * mGotInventoryListener - Listener that's called when we finish querying the items and subscriptions we own.
 */

IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
    public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
        if (AppConfig.DEBUG) Log.d (LOG_TAG, "Query inventory finished.");

        // Have we been disposed of in the meantime? If so, quit.
        if (pIabHelper == null) return;

        // Is it a failure?
        if (result.isFailure()) {
            complain("Failed to query inventory: " + result);
            onIabSetupFailed (pIabHelper);
            return;
        }

        if (AppConfig.DEBUG) Log.d (LOG_TAG, "Query inventory was successful.");

        // Call the methods 
        onIabSetupSucceeded (pIabHelper, result, inventory);

    }
};


/**
 * mConsumeFinishedListener - Listener that's called when consumption of the purchase item finishes
 */

IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
    public void onConsumeFinished(Purchase purchase, IabResult result) {
        if (AppConfig.DEBUG) 
           Log.d (LOG_TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);

        // if we were disposed of in the meantime, quit.
        if (pIabHelper == null) return;

        if (result.isSuccess ()) {

          // Use a synchronized block so there is only one update at a time.
          synchronized (pIabHelper) {
            onIabConsumeItemSucceeded (pIabHelper, purchase, result);
          } 
        } else {
          complain ("Did not handle purchase: " + purchase + " result: " + result);
          onIabConsumeItemFailed (pIabHelper);
        }
        if (AppConfig.DEBUG) Log.d (LOG_TAG, "End IAB consumption flow.");
    }
};

/**
 * mPurchaseFinishedListener - Listener that's called when a purchase is finished
 */

IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
    public void onIabPurchaseFinished (IabResult result, Purchase purchase) {
        if (AppConfig.DEBUG) Log.d (LOG_TAG, "Purchase finished: " + result + ", purchase: " + purchase);

        // if we were disposed of in the meantime, quit.
        if (pIabHelper == null) return;

        if (result.isFailure()) {
            complain("Purchase did not go through. " + result);
            onIabPurchaseFailed (pIabHelper, IAB_PURCHASE_FAILED);
            return;
        }
        if (!verifyDeveloperPayload(purchase)) {
            complain("Purchase did not go through. User verification failed.");
            onIabPurchaseFailed (pIabHelper, IAB_PURCHASE_FAILED_PAYLOAD_PROBLEM);
            return;
        }

        if (AppConfig.DEBUG) Log.d (LOG_TAG, "Purchase successful.");

        if (purchase.getSku().equals (SKU_PREMIUM)) {
            if (AppConfig.DEBUG) Log.d (LOG_TAG, "Purchase of premium gas finished. Consuming it ...");
            pIabHelper.consumeAsync (purchase, mConsumeFinishedListener);
        } else if (purchase.getSku().equals (SKU_GAS)) {
            if (AppConfig.DEBUG) Log.d (LOG_TAG, "Purchase is regular gas finished. Consuming it ...");
            pIabHelper.consumeAsync (purchase, mConsumeFinishedListener);
        } else if (purchase.getSku().equals (SKU_INFINITE_GAS)) {
            if (AppConfig.DEBUG) Log.d (LOG_TAG, "Purchase of infinite gase. Consuming it ...");
            pIabHelper.consumeAsync (purchase, mConsumeFinishedListener);
        }
    }
};

/**
 */
// Properties

/* Property IabHelper */
/**
 * This variable holds the value of the IabHelper property.
 */

   private IabHelper pIabHelper;

/**
 * Get the value of the IabHelper property.
 * 
 * @return IabHelper
 */

protected IabHelper getIabHelper () {
   //if (pIabHelper == null) {}
   return pIabHelper;
} // end getIabHelper

/**
 * Set the value of the IabHelper property.
 * 
 * @param newValue IabHelper
 */

protected void setIabHelper (IabHelper newValue) {
   pIabHelper = newValue;
} // end setIabHelper
/* end Property IabHelper */

/* Property ShowIabErrors */
/**
 * This variable holds the value of the ShowIabErrors property.
 */

   private boolean pShowIabErrors;

/**
 * Get the value of the ShowIabErrors property.
 * If this value is true, toast messages and dialog boxes are displayed to the
 * the user if errors occur doing IAB operations.
 * 
 * @return boolean
 */

public boolean getShowIabErrors () {
   return pShowIabErrors;
} // end getShowIabErrors

/**
 * Set the value of the ShowIabErrors property.
 * 
 * @param newValue boolean
 */

public void setShowIabErrors (boolean newValue) {
   pShowIabErrors = newValue;
} // end setShowIabErrors
/* end Property ShowIabErrors */

/**
 */
// Methods

/**
 * Alert the player that something is wrong using an AlertDialog.
 * <p>
 * If this method runs after the activity has been destroyed, the only action taken is to 
 * log the message.
 *
 * @param message String
 */

protected void alert(String message) {
    if (mDestroyed) {
       Log.d (LOG_TAG, "NOTE: IabActivity.alert called after onDestroy. Message: " + message);
       return;
    }

    AlertDialog.Builder bld = new AlertDialog.Builder(this);
    bld.setMessage(message);
    bld.setNeutralButton("OK", null);
    Log.d (LOG_TAG, "Showing alert dialog: " + message);
    bld.create().show();
}

/**
 * Log an error and alert the player that something is wrong.
 *
 * @param message String
 */

protected void complain(String message) {
    Log.e (LOG_TAG, "**** In-App Billing Error: " + message);
    alert("Error: " + message);
}

/**
 * Initiate the sequence of events needed to make a purchase.
 *
 * @param a Activity
 * @param sku String
 * @return void
 */

protected void launchInAppPurchaseFlow (Activity a, String sku) {
    launchInAppPurchaseFlow (a, sku, IabHelper.ITEM_TYPE_INAPP);
}

/**
 * Initiate the sequence of events needed to make a subscription purchase.
 *
 * @param a Activity
 * @param sku String
 * @return void
 */

protected void launchSubscriptionPurchaseFlow (Activity a, String sku) {
    launchInAppPurchaseFlow (a, sku, IabHelper.ITEM_TYPE_SUBS);
}

/**
 * Initiate the sequence of events needed to make a purchase.
 *
 * @param a Activity
 * @param sku String
 * @param itemType String - ITEM_TYPE_INAPP or ITEM_TYPE_SUBS
 * @return void
 */

protected void launchInAppPurchaseFlow (Activity a, String sku, String itemType) {
    IabHelper h = getIabHelper ();

    /* TODO: for security, generate your payload here for verification. See the comments on
     *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
     *        an empty string, but on a production app you should carefully generate this. */
    String payload = "";
    if (h != null) {
        h.launchPurchaseFlow (a, sku, itemType, RC_PURCHASE_REQUEST,
                              mPurchaseFinishedListener, payload);
    }
}

/** 
 * Handle the result of an activity that was started.
 * For this class, purchase requests are handled.
 * 
 * @param requestCode int - number identifying the type of request.
 * @return boolean - true means the purchase has been verified
 */

@Override protected void onActivityResult (int requestCode, int resultCode, Intent data) {
    switch (requestCode) {

      case RC_PURCHASE_REQUEST :
        //
        // Handle the purchase request here.
        // 
        if (AppConfig.DEBUG) Log.d (LOG_TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (pIabHelper != null) {
           if (!pIabHelper.handleActivityResult (requestCode, resultCode, data)) {
              // Not handled by the helper, so let other code do it.
              super.onActivityResult (requestCode, resultCode, data);
           }
        }
        break;

      default:
        // Anything else should be handled by other handlers.
        super.onActivityResult (requestCode, resultCode, data);
        break;
    }
}

/**
 * onCreate 
 *
 */

@Override protected void onCreate(Bundle savedInstanceState) {
   super.onCreate(savedInstanceState);
   mDestroyed = false;
}

/**
 * onDestroy
 * The final call you receive before your activity is destroyed. 
 * This can happen either because the activity is finishing (someone called finish() on it, 
 * or because the system is temporarily destroying this instance of the activity to save space. 
 * You can distinguish between these two scenarios with the isFinishing() method.
 *
 */

@Override public void onDestroy () {
    super.onDestroy ();

    // It is very important to destroy the IAB Helper. It holds onto to other stuff.
    if (pIabHelper != null) {
       if (AppConfig.DEBUG) Log.d (LOG_TAG, "Destroying IabHelper.");
       pIabHelper.dispose();
       pIabHelper = null;
    }
    mDestroyed = true;
}

/** 
 * Called when something fails in the purchase flow before the part where the item is consumed.
 *
 * <p> This is the place to reset the UI if something was done to indicate that a purchase has started.
 *
 * @param h IabHelper
 * @param errorNum int - error number from Constants
 * @return void
 */

void onIabPurchaseFailed (IabHelper h, int errorNum) {
}

/**
 * Show a resource string on the screen via Toast with Toast.LENGTH_LONG.
 * 
 * @param stringId int
 * @return void
 */

public void toast (int stringId)
{
    Resources res = getResources ();
    String msg = res.getString (stringId);
    Toast.makeText (this, msg, Toast.LENGTH_LONG).show ();
} // end toast

/**
 * Show a string on the screen via Toast with Toast.LENGTH_LONG.
 * 
 * @param msg String
 * @return void
 */

public void toast (String msg)
{
    Toast.makeText (getApplicationContext(), msg, Toast.LENGTH_LONG).show ();
} // end toast

/** 
 * Verifies the developer payload of a purchase. 
 * 
 * @param p Purchase
 * @return boolean - true means the purchase has been verified
 */

boolean verifyDeveloperPayload(Purchase p) {
    String payload = p.getDeveloperPayload();

    /*
     * TODO: verify that the developer payload of the purchase is correct. It will be
     * the same one that you sent when initiating the purchase.
     *
     * WARNING: Locally generating a random string when starting a purchase and
     * verifying it here might seem like a good approach, but this will fail in the
     * case where the user purchases an item on one device and then uses your app on
     * a different device, because on the other device you will not have access to the
     * random string you originally generated.
     *
     * So a good developer payload has these characteristics:
     *
     * 1. If two different users purchase an item, the payload is different between them,
     *    so that one user's purchase can't be replayed to another user.
     *
     * 2. The payload must be such that you can verify it even when the app wasn't the
     *    one who initiated the purchase flow (so that items purchased by the user on
     *    one device work on other devices owned by the user).
     *
     * Using your own server to store and verify developer payloads across app
     * installations is recommended.
     */

    return true;
}

/**
 * Set up for making purchases with IAB.
 * Part of that set up includes querying for purchases and querying for products listed.
 * If set up succeeds, method onIabSetupSuceeded is called with a filled in Inventory object.
 * If it fails, method onIabSetupFailed is called.
 * During set up, error messages are displayed if showErrors is true.
 * The messages are displayed using the toast method.
 *
 * <p> If you do not need product listings, set argument 1 to false. That saves time.
 * 
 * @param showListedSkus boolean - include the listed skus in the inventory
 * @param showErrors boolean - show errors with toast
 * @return boolean - true means success
 */

protected void setupIabHelper (final boolean showListedSkus, final boolean showErrors) {
    setShowIabErrors (showErrors);
    try {

        /* base64EncodedPublicKey should be YOUR APPLICATION'S PUBLIC KEY
         * (that you got from the Google Play developer console). This is not your
         * developer public key, it's the *app-specific* public key.
         *
         * Instead of just storing the entire literal string here embedded in the
         * program,  construct the key at runtime from pieces or
         * use bit manipulation (for example, XOR with some other string) to hide
         * the actual key.  The key itself is not secret information, but we don't
         * want to make it easy for an attacker to replace the public key with one
         * of their own and then fake messages from the server.
         */
        String base64EncodedPublicKey = "YOUR APPLICATION KEY GOES HERE";
        // Create the helper, passing it our context and the public key to verify signatures with
        if (AppConfig.DEBUG) Log.d (LOG_TAG, "Creating IAB helper.");
        IabHelper ih = new IabHelper(this, base64EncodedPublicKey);
        setIabHelper (ih);

        // enable debug logging (for a production application, you should set this to false).
        ih.enableDebugLogging (AppConfig.DEBUG, LOG_TAG);

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        if (AppConfig.DEBUG) Log.d (LOG_TAG, "Starting setup.");
        ih.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (AppConfig.DEBUG) Log.d(LOG_TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    if (pShowIabErrors)
                       complain ("Problem setting up in-app billing: " + result);
                       onIabSetupFailed (getIabHelper ());
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (pIabHelper == null) return;

                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                if (AppConfig.DEBUG) Log.d(LOG_TAG, "Setup successful. Querying inventory.");

                // Build up a list of the SKUs so we can get information on prices if we need to.
                ArrayList<String> skusToBeListed = null;
                if (showListedSkus) {
                   skusToBeListed = new ArrayList<String> ();
                   skusToBeListed.add (SKU_PREMIUM);
                   skusToBeListed.add (SKU_GAS);
                   skusToBeListed.add (SKU_INFINITE_GAS);
                }
                pIabHelper.queryInventoryAsync (true, skusToBeListed, mGotInventoryListener);
            }
        });

    } catch (Throwable ex) {

      // If we are debugging ...
      complain ("Exception while setting up in-app billing: " + ex.toString ());
      IabHelper ih = getIabHelper ();
      if (ih != null) onIabSetupFailed (ih);

      if (pShowIabErrors) toast (R.string.iab_setup_failed);
    }

}

/**
 */
// Methods for IabHelperListener. 
// Subclasses should call super method if they override the method.

/** 
 * Called when consumption of a purchase item fails.
 *
 * <p> If this class was set up to issue messages upon failure, there is probably
 * nothing else to be done.
 */

void onIabConsumeItemFailed (IabHelper h) {
}

/** 
 * Called when consumption of a purchase item succeeds.
 *
 * The default implementation here does not do anything. It is the responsibility of
 * subclasses to check the item purchased, consume it, and update state.
 *
 * @param h IabHelper - helper object
 * @param purchase Purchase
 * @param result IabResult
 */

void onIabConsumeItemSucceeded (IabHelper h, Purchase purchase, IabResult result) {
}

/** 
 * Called when setup fails and the inventory of items is not available. 
 *
 * <p> If this class was set up to issue messages upon failure, there is probably
 * nothing else to be done.
 */

void onIabSetupFailed (IabHelper h) {
}

/** 
 * Called when setup succeeds and the inventory of items is available. 
 *
 * <p> The code here checks to see if there are any left over purchases of coins
 * that were not handled. In IAB terms, the purchases are consumeable. When you 
 * purchase a coin pack, for instance, the coins are consumed and turned into
 * coins in the game.
 *
 * <p> Subclasses should implement this class if the they want to update their UI
 * when setup completes. They should call super.onIabSetupSucceeded first.
 *
 * @param h IabHelper - helper object
 * @param result IabResult
 * @param inventory Inventory
 *
 */

void onIabSetupSucceeded (IabHelper h, IabResult result, Inventory inventory) {
    // Check to see which upgrades we have?

    //
    // Check for the in-app purchases of items.
    // (Do this here in the superclass of all the other classes so it is done only once.
    //  Note that several global variables are set here.)
    //

    // Do we have the premium upgrade?
    Purchase premiumPurchase = inventory.getPurchase(SKU_PREMIUM);
    mIsPremium = (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase));
    if (AppConfig.DEBUG) Log.d (LOG_TAG, "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));

    // Do we have the infinite gas plan?
    Purchase infiniteGasPurchase = inventory.getPurchase(SKU_INFINITE_GAS);
    mSubscribedToInfiniteGas = (infiniteGasPurchase != null &&
            verifyDeveloperPayload(infiniteGasPurchase));
    if (AppConfig.DEBUG) Log.d (LOG_TAG, "User " + (mSubscribedToInfiniteGas ? "HAS" : "DOES NOT HAVE")
                + " infinite gas subscription.");
    //if (mSubscribedToInfiniteGas) mTank = TANK_MAX;

    // Check for gas delivery -- if we own gas, we should fill up the tank immediately
    Purchase gasPurchase = inventory.getPurchase(SKU_GAS);
    if (gasPurchase != null && verifyDeveloperPayload(gasPurchase)) {
        if (AppConfig.DEBUG) Log.d (LOG_TAG, "We have gas. Consuming it.");
        pIabHelper.consumeAsync (gasPurchase, mConsumeFinishedListener);
        return;
    }

    //if (mSubscribedToInfiniteGas) mTank = TANK_MAX;   // Done in MainActivity

    if (AppConfig.DEBUG) Log.d (LOG_TAG, "IabActivity.onIabSetupSucceeded completed. Subclass should update UI.");

}

} // end class
