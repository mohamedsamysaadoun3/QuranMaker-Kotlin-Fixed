package hazem.nurmontage.videoquran.ui.billing

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Pair
import android.view.View
import android.widget.Toast
import androidx.activity.EdgeToEdge
import androidx.activity.OnBackPressedCallback
import androidx.core.graphics.Insets
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import hazem.nurmontage.videoquran.R
import hazem.nurmontage.videoquran.adapter.AboutAdabters
import hazem.nurmontage.videoquran.core.base.BaseActivity
import hazem.nurmontage.videoquran.ui.settings.ThanksYouActivity
import hazem.nurmontage.videoquran.utils.AppUtils
import hazem.nurmontage.videoquran.utils.LocaleHelper
import hazem.nurmontage.videoquran.utils.NonScrollableLinearLayoutManager
import hazem.nurmontage.videoquran.utils.PriceFormatter
import hazem.nurmontage.videoquran.utils.ScreenUtils
import hazem.nurmontage.videoquran.views.widget.ButtonCustumFont
import hazem.nurmontage.videoquran.views.text.TextCustumFont

/**
 * Activity that allows the user to support the developer through
 * in-app purchases (donations).  Four SKUs are offered at different
 * price tiers.  The Google Play Billing Library handles the purchase
 * flow; upon successful purchase the user is shown a thank-you screen.
 */
class SupportBillingActivity : BaseActivity(), PurchasesUpdatedListener {

    // ──────────────────────────────────────────────
    //  SKU constants
    // ──────────────────────────────────────────────

    companion object {
        private const val PRODUCT_ID_10 = "sku.nurmontage.min"
        private const val PRODUCT_ID_50 = "sku.nurmontage.medium"
        private const val PRODUCT_ID_100 = "sku.nurmontage.mmedium"
        private const val PRODUCT_ID_1000 = "sku.nurmontage.max"
    }

    // ──────────────────────────────────────────────
    //  State
    // ──────────────────────────────────────────────

    private var productIdCurrent: String = PRODUCT_ID_50
    private var priceSelect: Int = R.id.view_50

    private val productDetailsMap = mutableMapOf<String, ProductDetails>()

    // ──────────────────────────────────────────────
    //  Views
    // ──────────────────────────────────────────────

    private lateinit var billingClient: BillingClient
    private lateinit var btnLaunch: ButtonCustumFont
    private lateinit var mResources: Resources
    private lateinit var viewPrice10: ButtonCustumFont
    private lateinit var viewPrice50: ButtonCustumFont
    private lateinit var viewPrice100: ButtonCustumFont
    private lateinit var viewPrice1000: ButtonCustumFont

    /** Handles the back-press by finishing the activity. */
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
        }
    }

    // ──────────────────────────────────────────────
    //  Lifecycle
    // ──────────────────────────────────────────────

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EdgeToEdge.enable(this)
        setContentView(R.layout.activity_support_billing)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        // Edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, windowInsets ->
            val insets: Insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }

        // Light system bars on white background
        setStatusBarColor(-1)
        setNavigationBarColor(-1)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = true
        insetsController.isAppearanceLightNavigationBars = true

        wakeLockAcquire()

        mResources = resources
        if (mResources == null) {
            finish()
        }

        init()
        initImgBilling()

        // Back button
        findViewById<View>(R.id.btn_on_back).setOnClickListener {
            onBackPressedCallback.handleOnBackPressed()
        }

        // Initialise BillingClient
        billingClient = BillingClient.newBuilder(this)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        startBillingConnection()
    }

    // ──────────────────────────────────────────────
    //  UI initialisation
    // ──────────────────────────────────────────────

    private fun initImgBilling() {
        ScreenUtils.getScreenWidth(this)
    }

    private fun init() {
        val language = LocaleHelper.getLanguage(applicationContext)
        val tvAya: TextCustumFont = findViewById(R.id.tv_aya)
        tvAya.text = mResources.getString(R.string.tittle_billing)
        if (language == "ar") {
            tvAya.textSize = 16.0f
        }

        val gravity = if (language == "ar") 5 else GravityCompat.START

        val list = arrayListOf<AboutAdabters.ModelAbout>(
            AboutAdabters.ModelAbout(
                14,
                Pair("<font color='#000000'>${mResources.getString(R.string.about_question_1)}</font>", gravity)
            ),
            AboutAdabters.ModelAbout(14, Pair("\n", gravity)),
            AboutAdabters.ModelAbout(
                14,
                Pair("<font color='#000000'>${mResources.getString(R.string.about_question_2)}</font>", gravity)
            ),
            AboutAdabters.ModelAbout(14, Pair("\n", gravity)),
            AboutAdabters.ModelAbout(
                14,
                Pair("<font color='#000000'>${mResources.getString(R.string.about_question_3)}</font>", gravity)
            ),
            AboutAdabters.ModelAbout(14, Pair("\n\n", gravity)),
            AboutAdabters.ModelAbout(
                14,
                Pair("<font color='#000000'>${mResources.getString(R.string.about_no_ads)}</font>", gravity)
            ),
            AboutAdabters.ModelAbout(14, Pair("\n", gravity)),
            AboutAdabters.ModelAbout(
                14,
                Pair("<font color='#000000'>${mResources.getString(R.string.about_cost_explanation)}</font>", gravity)
            ),
        )

        val screenWidth = ScreenUtils.getScreenWidth(this)
        val recyclerView: RecyclerView = findViewById(R.id.rv)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = NonScrollableLinearLayoutManager(this)
        recyclerView.adapter = AboutAdabters(
            this,
            AppUtils.getAppVersionName(this),
            list,
            screenWidth,
            (screenWidth * 0.4f).toInt()
        )

        // Price-tier buttons
        viewPrice10 = findViewById(R.id.view_10)
        viewPrice50 = findViewById(R.id.view_50)
        viewPrice100 = findViewById(R.id.view_100)
        viewPrice1000 = findViewById(R.id.view_1000)
        btnLaunch = findViewById(R.id.btn_launch)

        viewPrice10.setOnClickListener {
            productIdCurrent = PRODUCT_ID_10
            updatePrice(viewPrice10.text.toString(), R.id.view_10, priceSelect)
        }
        viewPrice50.setOnClickListener {
            productIdCurrent = PRODUCT_ID_50
            updatePrice(viewPrice50.text.toString(), R.id.view_50, priceSelect)
        }
        viewPrice100.setOnClickListener {
            productIdCurrent = PRODUCT_ID_100
            updatePrice(viewPrice100.text.toString(), R.id.view_100, priceSelect)
        }
        viewPrice1000.setOnClickListener {
            productIdCurrent = PRODUCT_ID_1000
            updatePrice(viewPrice1000.text.toString(), R.id.view_1000, priceSelect)
        }

        btnLaunch.setOnClickListener {
            launchPurchaseFlow(productIdCurrent)
        }
    }

    private fun updatePrice(priceText: String, selectedId: Int, previousId: Int) {
        if (selectedId == previousId) return
        btnLaunch?.setText(
            String.format(mResources.getString(R.string.btn_launch_billing), priceText)
        )
        findViewById<View>(selectedId).setBackgroundResource(R.drawable.item_billing_select)
        findViewById<View>(previousId).setBackgroundResource(R.drawable.item_billing)
        priceSelect = selectedId
    }

    // ──────────────────────────────────────────────
    //  Billing connection
    // ──────────────────────────────────────────────

    /**
     * Starts the BillingClient connection to Google Play.
     * On success it queries available products and pending purchases.
     * On disconnection it retries automatically.
     */
    private fun startBillingConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProducts()
                    queryPurchases()
                }
            }

            override fun onBillingServiceDisconnected() {
                startBillingConnection()
            }
        })
    }

    // ──────────────────────────────────────────────
    //  Product queries
    // ──────────────────────────────────────────────

    /**
     * Queries the four donation SKUs from Google Play.
     */
    private fun queryProducts() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID_10)
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID_50)
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID_100)
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID_1000)
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
        )

        billingClient.queryProductDetailsAsync(
            QueryProductDetailsParams.newBuilder().setProductList(productList).build(),
            object : ProductDetailsResponseListener {
                override fun onProductDetailsResponse(
                    billingResult: BillingResult,
                    productDetailsList: List<ProductDetails>?
                ) {
                    if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) return
                    if (productDetailsList == null) return
                    for (details in productDetailsList) {
                        productDetailsMap[details.productId] = details
                        val capturedDetails = details
                        runOnUiThread { updateUI(capturedDetails) }
                    }
                }
            }
        )
    }

    /**
     * Queries already-owned purchases so they can be acknowledged
     * or consumed if they were not already.
     */
    private fun queryPurchases() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            object : PurchasesResponseListener {
                override fun onQueryPurchasesResponse(
                    billingResult: BillingResult,
                    purchases: List<Purchase>
                ) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        for (purchase in purchases) {
                            handlePurchase(purchase)
                        }
                    }
                }
            }
        )
    }

    // ──────────────────────────────────────────────
    //  UI update from product details
    // ──────────────────────────────────────────────

    /**
     * Updates the price label on the corresponding button once
     * the product details have been fetched from Google Play.
     */
    private fun updateUI(productDetails: ProductDetails) {
        val productId = productDetails.productId
        val formatPrice = PriceFormatter.formatPrice(
            productDetails.oneTimePurchaseOfferDetails?.formattedPrice ?: ""
        )
        when (productId) {
            PRODUCT_ID_10 -> viewPrice10.text = formatPrice
            PRODUCT_ID_50 -> {
                viewPrice50.text = formatPrice
                btnLaunch.setText(
                    String.format(
                        mResources.getString(R.string.btn_launch_billing),
                        viewPrice50.text.toString()
                    )
                )
            }
            PRODUCT_ID_100 -> viewPrice100.text = formatPrice
            PRODUCT_ID_1000 -> viewPrice1000.text = formatPrice
        }
    }

    // ──────────────────────────────────────────────
    //  Purchase flow
    // ──────────────────────────────────────────────

    /**
     * Launches the Google Play purchase dialog for the given SKU.
     */
    private fun launchPurchaseFlow(productId: String) {
        val productDetails = productDetailsMap[productId] ?: return
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        billingClient.launchBillingFlow(this, billingFlowParams)
    }

    // ──────────────────────────────────────────────
    //  PurchasesUpdatedListener
    // ──────────────────────────────────────────────

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else {
            // Response code is available for logging if needed
            billingResult.responseCode
        }
    }

    // ──────────────────────────────────────────────
    //  Purchase handling
    // ──────────────────────────────────────────────

    /**
     * Navigates to the thank-you screen and shows the donation amount.
     */
    private fun thnks() {
        val intent = Intent(this, ThanksYouActivity::class.java).apply {
            putExtra("price", (findViewById<View>(priceSelect) as ButtonCustumFont).text.toString())
        }
        startActivity(intent)
    }

    /**
     * Handles a completed [Purchase]:
     * - **Purchased** (state 1): navigates to thank-you, then consumes.
     * - **Pending** (state 2): shows a pending toast.
     * - **Unknown**: shows an unknown-state toast.
     */
    private fun handlePurchase(purchase: Purchase) {
        when (purchase.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                thnks()
                if (!purchase.isAcknowledged) {
                    billingClient.consumeAsync(
                        ConsumeParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()
                    ) { billingResult, _ ->
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            Toast.makeText(
                                this,
                                "Purchase consumed successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this,
                                "Error consuming purchase",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            Purchase.PurchaseState.PENDING -> {
                Toast.makeText(this, "Purchase is pending", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this, "Purchase is in unknown state", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
