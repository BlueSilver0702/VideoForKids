/* Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wglxy.example.trivialdrive2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.wglxy.example.trivialdrive2.R;
import com.wglxy.example.trivialdrive2.util.IabHelper;
import com.wglxy.example.trivialdrive2.util.IabResult;
import com.wglxy.example.trivialdrive2.util.Inventory;
import com.wglxy.example.trivialdrive2.util.Purchase;

import static com.wglxy.example.trivialdrive2.Constants.*;

/**
 * Example game using in-app billing version 3.
 *
 * Before attempting to run this sample, please read the README file. It
 * contains important information on how to set up this project.
 *
 * All the game-specific logic is implemented here in MainActivity, while the
 * general-purpose boilerplate that can be reused in any app is provided in the
 * classes in the util/ subdirectory. When implementing your own application,
 * you can copy over util/*.java to make use of those utility classes.
 *
 * This game is a simple "driving" game where the player can buy gas
 * and drive. The car has a tank which stores gas. When the player purchases
 * gas, the tank fills up (1/4 tank at a time). When the player drives, the gas
 * in the tank diminishes (also 1/4 tank at a time).
 *
 * The user can also purchase a "premium upgrade" that gives them a red car
 * instead of the standard blue one (exciting!).
 *
 * The user can also purchase a subscription ("infinite gas") that allows them
 * to drive without using up any gas while that subscription is active.
 *
 * It's important to note the consumption mechanics for each item.
 *
 * PREMIUM: the item is purchased and NEVER consumed. So, after the original
 * purchase, the player will always own that item. The application knows to
 * display the red car instead of the blue one because it queries whether
 * the premium "item" is owned or not.
 *
 * INFINITE GAS: this is a subscription, and subscriptions can't be consumed.
 *
 * GAS: when gas is purchased, the "gas" item is then owned. We consume it
 * when we apply that item's effects to our app's world, which to us means
 * filling up 1/4 of the tank. This happens immediately after purchase!
 * It's at this point (and not when the user drives) that the "gas"
 * item is CONSUMED. Consumption should always happen when your game
 * world was safely updated to apply the effect of the purchase. So,
 * in an example scenario:
 *
 * BEFORE:      tank at 1/2
 * ON PURCHASE: tank at 1/2, "gas" item is owned
 * IMMEDIATELY: "gas" is consumed, tank goes to 3/4
 * AFTER:       tank at 3/4, "gas" item NOT owned any more
 *
 * Another important point to notice is that it may so happen that
 * the application crashed (or anything else happened) after the user
 * purchased the "gas" item, but before it was consumed. That's why,
 * on startup, we check if we own the "gas" item, and, if so,
 * we have to apply its effects to our world and consume it. This
 * is also very important!
 *
 * @author Bruno Oliveira (Google)
 *
 * ---
 *
 * MODIFIED - MAY 2015
 *
 * This class was modified by Bill Lahti so more of the handling of in-app billing was in superclass IabActivity.
 * See Bill Lahti's blog for more information: blahti.wordpress.com.
 * Blog article: "Tutorial: How to Implement In-app Billing in Android – Part 2"
 *
 * ---
 */

public class MainActivity extends IabActivity {

/**
 */
// Variables

// Debug tag, for logging
static final String TAG = Constants.LOG_IAB;

// Helper object for in-app billing.
IabHelper mHelper = null;

// Current amount of gas in tank, in units
protected int mTank;


/**
 */
// Methods

/**
 * Do actions for when the activity is created.
 */
 
@Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Start setup of in-app billing. Note that this work is now done in the IabActivity, which
    // is the superclass of this method.
    setupIabHelper (true, true);

    // Set a variable for convenient access to the iab helper object.
    mHelper = getIabHelper ();

    // load game data
    loadData();
    updateUi ();

    // enable debug logging (for a production application, you should set this to false).
    mHelper.enableDebugLogging(true);

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

@Override void onIabPurchaseFailed (IabHelper h, int errorNum) {
    // We did set up in such a way so that error messages have already been display (with complain method).
    // So all we have to do is remove the "waiting" indicator.
    if (errorNum != 0) setWaitScreen (false);
    updateUi();
    
}

/**
 * User clicked the "Buy Gas" button
 */
 
public void onBuyGasButtonClicked(View arg0) {
    Log.d(TAG, "Buy gas button clicked.");

    if (mSubscribedToInfiniteGas) {
        complain("No need! You're subscribed to infinite gas. Isn't that awesome?");
        return;
    }

    if (mTank >= TANK_MAX) {
        complain("Your tank is full. Drive around a bit!");
        return;
    }

    // launch the gas purchase UI flow.
    // We will be notified of completion via mPurchaseFinishedListener
    setWaitScreen(true);
    Log.d(TAG, "Launching purchase flow for gas.");

    // The steps needed to complete a purchase are done in code in the IabActivity superclass.
    // IabActivity provides standard handling and calls back to this class.
    launchInAppPurchaseFlow (this, SKU_GAS);


    /* TODO: for security, generate your payload here for verification. See the comments on
     *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
     *        an empty string, but on a production app you should carefully generate this. */

    /*
    String payload = "";

    mHelper.launchPurchaseFlow(this, SKU_GAS, RC_PURCHASE_REQUEST,
            mPurchaseFinishedListener, payload);
    */
}

/**
 * User clicked the "Upgrade to Premium" button.
 */
 
public void onUpgradeAppButtonClicked (View arg0) {
    Log.d(TAG, "Upgrade button clicked; launching purchase flow for upgrade.");
    setWaitScreen(true);

    launchInAppPurchaseFlow (this, SKU_PREMIUM);

    /* TODO: for security, generate your payload here for verification. See the comments on
     *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
     *        an empty string, but on a production app you should carefully generate this. */
/*     
    String payload = "";

    mHelper.launchPurchaseFlow(this, SKU_PREMIUM, RC_PURCHASE_REQUEST,
            mPurchaseFinishedListener, payload);
*/            
}

// "Subscribe to infinite gas" button clicked. Explain to user, then start purchase
// flow for subscription.
public void onInfiniteGasButtonClicked(View arg0) {
    if (!mHelper.subscriptionsSupported()) {
        complain("Subscriptions not supported on your device yet. Sorry!");
        return;
    }

    /* TODO: for security, generate your payload here for verification. See the comments on
     *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
     *        an empty string, but on a production app you should carefully generate this. */
    String payload = "";

    setWaitScreen(true);

    launchSubscriptionPurchaseFlow (this, SKU_INFINITE_GAS);
    /* (original code)
    Log.d(TAG, "Launching purchase flow for infinite gas subscription.");
    mHelper.launchPurchaseFlow(this,
            SKU_INFINITE_GAS, IabHelper.ITEM_TYPE_SUBS,
            RC_PURCHASE_REQUEST, mPurchaseFinishedListener, payload);
    */
}

@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
    if (mHelper == null) return;

    // Pass on the activity result to the helper for handling
    if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
        // not handled, so handle it ourselves (here's where you'd
        // perform any handling of activity results not related to in-app
        // billing...
        super.onActivityResult(requestCode, resultCode, data);
    }
    else {
        Log.d(TAG, "onActivityResult handled by IABUtil.");
    }
}

// (wgl, May 2015)
// The next two variables are the original listener objects.
// They are no longer used. See superclass IabActivity for the current ones.

    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListenerOLD = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                complain("Error purchasing: " + result);
                updateUi();
                setWaitScreen(false);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                complain("Error purchasing. Authenticity verification failed.");
                updateUi();
                setWaitScreen(false);
                return;
            }

            Log.d(TAG, "Purchase successful.");

            if (purchase.getSku().equals(SKU_GAS)) {
                // bought 1/4 tank of gas. So consume it.
                Log.d(TAG, "Purchase is gas. Starting gas consumption.");
                mHelper.consumeAsync(purchase, mConsumeFinishedListener);
            }
            else if (purchase.getSku().equals(SKU_PREMIUM)) {
                // bought the premium upgrade!
                Log.d(TAG, "Purchase is premium upgrade. Congratulating user.");
                alert("Thank you for upgrading to premium!");
                mIsPremium = true;
                updateUi();
                setWaitScreen(false);
            }
            else if (purchase.getSku().equals(SKU_INFINITE_GAS)) {
                // bought the infinite gas subscription
                Log.d(TAG, "Infinite gas subscription purchased.");
                alert("Thank you for subscribing to infinite gas!");
                mSubscribedToInfiniteGas = true;
                mTank = TANK_MAX;
                updateUi();
                setWaitScreen(false);
            }
        }
    };

    // Called when consumption is complete
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListenerOLD = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            // We know this is the "gas" sku because it's the only one we consume,
            // so we don't check which sku was consumed. If you have more than one
            // sku, you probably should check...
            if (result.isSuccess()) {
                // successfully consumed, so we apply the effects of the item in our
                // game world's logic, which in our case means filling the gas tank a bit
                Log.d(TAG, "Consumption successful. Provisioning.");
                mTank = mTank == TANK_MAX ? TANK_MAX : mTank + 1;
                saveData();
                alert("You filled 1/4 tank. Your tank is now " + String.valueOf(mTank) + "/4 full!");
            }
            else {
                complain("Error while consuming: " + result);
            }
            updateUi();
            setWaitScreen(false);
            Log.d(TAG, "End consumption flow.");
        }
    };

// Methods

    // Drive button clicked. Burn gas!
    public void onDriveButtonClicked(View arg0) {
        Log.d(TAG, "Drive button clicked.");
        if (!mSubscribedToInfiniteGas && mTank <= 0) alert("Oh, no! You are out of gas! Try buying some!");
        else {
            if (!mSubscribedToInfiniteGas) --mTank;
            saveData();
            alert("Vroooom, you drove a few miles.");
            updateUi();
            Log.d(TAG, "Vrooom. Tank is now " + mTank);
        }
        updateUi ();
    }

    // We're being destroyed. It's important to dispose of the helper here!
    @Override
    public void onDestroy() {
        super.onDestroy();

        // very important:
        Log.d(TAG, "Destroying helper.");
        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
        }
    }

    // updates UI to reflect model
    public void updateUi() {
        // update the car color to reflect premium status or lack thereof
        ((ImageView)findViewById(R.id.free_or_premium)).setImageResource(mIsPremium ? R.drawable.premium : R.drawable.free);

        // "Upgrade" button is only visible if the user is not premium
        findViewById(R.id.upgrade_button).setVisibility(mIsPremium ? View.GONE : View.VISIBLE);

        // "Get infinite gas" button is only visible if the user is not subscribed yet
        findViewById(R.id.infinite_gas_button).setVisibility(mSubscribedToInfiniteGas ?
                View.GONE : View.VISIBLE);

        // update gas gauge to reflect tank status
        if (mSubscribedToInfiniteGas) {
            ((ImageView)findViewById(R.id.gas_gauge)).setImageResource(R.drawable.gas_inf);
        }
        else {
            int index = mTank >= TANK_RES_IDS.length ? TANK_RES_IDS.length - 1 : mTank;
            ((ImageView)findViewById(R.id.gas_gauge)).setImageResource(TANK_RES_IDS[index]);
        }
    }

    // Enables or disables the "please wait" screen.
    void setWaitScreen(boolean set) {
        findViewById(R.id.screen_main).setVisibility(set ? View.GONE : View.VISIBLE);
        findViewById(R.id.screen_wait).setVisibility(set ? View.VISIBLE : View.GONE);
    }

    void saveData() {

        /*
         * WARNING: on a real application, we recommend you save data in a secure way to
         * prevent tampering. For simplicity in this sample, we simply store the data using a
         * SharedPreferences.
         */

        SharedPreferences.Editor spe = getPreferences(MODE_PRIVATE).edit();
        spe.putInt("tank", mTank);
        spe.commit();
        Log.d(TAG, "Saved data: tank = " + String.valueOf(mTank));
    }

    void loadData() {
        SharedPreferences sp = getPreferences(MODE_PRIVATE);
        mTank = sp.getInt("tank", 2);
        Log.d(TAG, "Loaded data: tank = " + String.valueOf(mTank));
    }

/**
 */
// Methods for IabHelperListener. 
// Subclasses should call the superclass method if they override any of these methods.

/** 
 * Called when consumption of a purchase item fails.
 *
 * <p> If this class was set up to issue messages upon failure, there is probably
 * nothing else to be done.
 */

void onIabConsumeItemFailed (IabHelper h) {
    super.onIabConsumeItemFailed (h);
    
    // Do whatever you need to in the ui to indicate that consuming a purchase failed.
    updateUi();
    setWaitScreen(false);
    
}

/** 
 * Called when consumption of a purchase item succeeds.
 *
 * SKU_GAS is the only consumable ite,. When it is purchased, this method gets called.
 * So this is the place where the tank is filled.
 *
 * @param h IabHelper - helper object
 * @param purchase Purchase
 * @param result IabResult
 */

void onIabConsumeItemSucceeded (IabHelper h, Purchase purchase, IabResult result) {
    super.onIabConsumeItemSucceeded (h, purchase, result);

    // Update the state of the app and the ui to show the item we purchased and consumed.
    String purchaseSku = purchase.getSku ();    
    if (purchaseSku.equals(SKU_GAS)) {
       if (result.isSuccess()) {
          // successfully consumed, so we apply the effects of the item in our
          // game world's logic, which in our case means filling the gas tank a bit
          Log.d(TAG, "Consumption successful. Provisioning.");
          mTank = mTank == TANK_MAX ? TANK_MAX : mTank + 1;
          saveData();
          alert("You filled 1/4 tank. Your tank is now " + String.valueOf(mTank) + "/4 full!");
       } else {
         complain("Error while consuming regular gas: " + result);
       }

    }

    // Original code did some processing here. I moved this to IabActivity. (wgl, May 2015)
    /*
    else if (purchase.getSku().equals(SKU_PREMIUM)) {
      // bought the premium upgrade!
      Log.d(TAG, "Purchase is premium upgrade. Congratulating user.");
      alert("Thank you for upgrading to premium!");
      mIsPremium = true;
      updateUi();
      setWaitScreen(false);
    } else if (purchase.getSku().equals(SKU_INFINITE_GAS)) {

      // bought the infinite gas subscription
      Log.d(TAG, "Infinite gas subscription purchased.");
      alert("Thank you for subscribing to infinite gas!");
      mSubscribedToInfiniteGas = true;
      mTank = TANK_MAX;
      updateUi();
      setWaitScreen(false);
    }
    */

    updateUi();
    setWaitScreen(false);

}

/** 
 * Called when setup fails and the inventory of items is not available. 
 *
 * <p> If this class was set up to issue messages upon failure, there is probably
 * nothing else to be done.
 */

void onIabSetupFailed (IabHelper h) {
    super.onIabSetupFailed (h);

    // This would be where to change the ui in the event of a set up error.
}

/** 
 * Called when setup succeeds and the inventory of items is available. 
 *
 * @param h IabHelper - helper object
 * @param result IabResult
 * @param inventory Inventory
 *
 */

void onIabSetupSucceeded (IabHelper h, IabResult result, Inventory inventory) {
    super.onIabSetupSucceeded (h, result, inventory);

    // The superclass setup method checks to see what has been purchased and what has been subscribed to.
    // Premium and infinite gas are handled here. If there was a regular gas purchase, steps to consume
    // it (via an async call to consume) were started in the superclass.

    if (mSubscribedToInfiniteGas) mTank = TANK_MAX;

    if (mIsPremium) {
       // FIX THIS
    }
    
    updateUi();
    setWaitScreen(false);

}

} // end class MainActivity
