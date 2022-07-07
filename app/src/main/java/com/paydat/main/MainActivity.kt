package com.paydat.main

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.ktx.Firebase
import com.paydat.R
import com.paydat.data.entities.model.main.NavigationDrawerModel
import com.paydat.data.entities.model.main.NavigationType
import com.paydat.data.repositories.PreferenceManager
import com.paydat.data.repositories.user.UserType
import com.paydat.databinding.ActivityMainBinding
import com.paydat.databinding.ItemHeaderBinding
import com.paydat.databinding.ItemMenuSideBarProfileBinding
import com.paydat.domain.BaseUseCase
import com.paydat.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationDrawerAdapter.Callback, LogoutCallback {
    @Inject
    lateinit var preferenceManager: PreferenceManager

    @Inject
    lateinit var permissionManager: PermissionManager
    private lateinit var binding: ActivityMainBinding
    private lateinit var bindingHeader: ItemHeaderBinding
    private lateinit var bindingProfile: ItemMenuSideBarProfileBinding
    private lateinit var progressDialog: ProgressDialog

    private val viewModel: MainActivityViewModel by viewModels()
    private lateinit var navController: NavController
    private lateinit var adapter: NavigationDrawerAdapter
    private var actionBarDrawerToggle: ActionBarDrawerToggle? = null

    private var list = ArrayList<NavigationDrawerModel>()
    private lateinit var uri: Uri

    private val cameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            navigate(uri)
            toolbarStatus(View.VISIBLE, DrawerLayout.LOCK_MODE_UNLOCKED)
            actionBar()
            closeDrawer()
        } else {
            val dialog = AlertDialogs(
                this,
                getString(R.string.dialog_info),
                getString(R.string.camera_permission_desc),
                isPositiveButton = true,
                isCancelButton = false,
                positive_button_text = getString(R.string.btn_ok),
                cancel_button_text = getString(R.string.cancel),
                listener = object : AlertDialogsListener {

                    override fun onClickClose() {
                    }

                    override fun onClickPositive() {

                    }
                }
            )
            dialog.show()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog()
        actionBar()
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        bindingHeader = binding.header
        bindingProfile = binding.profile

        toggleSelection()

        navController.addOnDestinationChangedListener(destinationChangeListener)

        viewModel.screenHandler()
        viewModel.stateFlow.onEach {
            handle(it)
        }.launchIn(lifecycleScope)

        notification()
        initializeList()
        listeners()
    }

    private fun notification() {
//        val uri = intent.data
//        if (uri != null) {
//            val path = uri.toString()
//            Toast.makeText(this, path, Toast.LENGTH_LONG).show()
//        }

        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener(
                this
            ) { pendingDynamicLinkData ->
                // Get deep link from result (may be null if no link is found)
                var deepLink: Uri? = null
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                    if (deepLink != null) {
                        val intentData = intent.data
                        if (intentData != null) {
                            val code = intent.getStringExtra("productId") // always returns null

                            android.util.Log.d("=========", "notification: $code")
                        }

                        android.util.Log.d("=========", "notification: $deepLink")
                    }
                }


                // Handle the deep link. For example, open the linked
                // content, or apply promotional credit to the user's
                // account.
                // ...

                // ...
            }
            .addOnFailureListener(
                this
            ) { e -> Log.w("========", "getDynamicLink:onFailure", e) }
    }

    private fun toggleSelection() {
        if (preferenceManager.accountType == UserType.USER_TYPE_INDIVIDUAL.type) {
            bindingProfile.rbIndividual.isChecked = true
        } else {
            bindingProfile.rbMerchant.isChecked = true
        }
    }

    private fun listeners() {
        bindingProfile.itemAction.setOnClickListener {
            navigation(Paths.URI_PROFILE)
        }

        bindingProfile.rbIndividual.setOnClickListener {
            viewModel.switchAccount(UserType.USER_TYPE_INDIVIDUAL.type)
            accountClickEvent()
        }

        bindingProfile.rbMerchant.setOnClickListener {
            viewModel.switchAccount(UserType.USER_TYPE_MERCHANT.type)
            accountClickEvent()
        }
    }

    private fun accountClickEvent() {
//        bindingProfile.radioGroupAccountType.setOnCheckedChangeListener { _, checkedId ->
//            val radioButton: View = bindingProfile.radioGroupAccountType.findViewById(checkedId)
//            when (bindingProfile.radioGroupAccountType.indexOfChild(radioButton)) {
//                0 -> {
//                    viewModel.switchAccount(UserType.USER_TYPE_INDIVIDUAL.type)
//                }
//                1 -> {
//                    viewModel.switchAccount(UserType.USER_TYPE_MERCHANT.type)
//                }
//            }
//        }
    }

    private val destinationChangeListener =
        NavController.OnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_splash -> {
                    toolbarStatus(View.GONE, DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                }

                R.id.navigation_welcome -> {
                    toolbarStatus(View.VISIBLE, DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    binding.header.toolbar.navigationIcon = null
                }

                R.id.navigation_login -> {
                    toolbarStatus(View.VISIBLE, DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    setToolbarWithBackPressButton()
                }

                R.id.navigation_forgot -> {
                    toolbarStatus(View.VISIBLE, DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    setToolbarWithBackPressButton()
                }

                R.id.navigation_register -> {
                    toolbarStatus(View.VISIBLE, DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    setToolbarWithBackPressButton()
                }

                R.id.navigation_profile -> {
                    toolbarStatus(View.VISIBLE, DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    setToolbarWithBackPressButton()
                }

                R.id.navigation_sales -> {
                    toolbarStatus(View.VISIBLE, DrawerLayout.LOCK_MODE_UNLOCKED)
                    actionBar()
                    closeDrawer()
                }

                R.id.navigation_scan -> {
                    setUserProfile()
                    initializeList()
                    toolbarStatus(View.VISIBLE, DrawerLayout.LOCK_MODE_UNLOCKED)
                    actionBar()
                    closeDrawer()
                }

                R.id.navigation_authorise -> {
                    toolbarStatus(View.VISIBLE, DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    setToolbarWithBackPressButton()
                }

                R.id.navigation_payment -> {
                    toolbarStatus(View.VISIBLE, DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    setToolbarWithBackPressButton()
                }

                R.id.navigation_products -> {
                    toolbarStatus(View.VISIBLE, DrawerLayout.LOCK_MODE_UNLOCKED)
                    actionBar()
                    closeDrawer()
                }

                R.id.navigation_my_qr -> {
                    toolbarStatus(View.VISIBLE, DrawerLayout.LOCK_MODE_UNLOCKED)
                    actionBar()
                    closeDrawer()
                }

                R.id.navigation_sell -> {
                    setUserProfile()
                    initializeList()
                    toolbarStatus(View.VISIBLE, DrawerLayout.LOCK_MODE_UNLOCKED)
                    actionBar()
                    closeDrawer()
                }

                R.id.navigation_generate_qr -> {
                    toolbarStatus(View.VISIBLE, DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    setToolbarWithBackPressButton()
                }

                else -> {
                    toolbarStatus(View.VISIBLE, DrawerLayout.LOCK_MODE_UNLOCKED)
                    setToolbarWithBackPressButton()
                }
            }
            hideKeyBoard()
        }

    private fun toolbarStatus(status: Int, mode: Int) {
        binding.apply {
            header.root.visibility = status
            drawerLayout.setDrawerLockMode(mode)
        }
    }

    private fun setToolbarWithBackPressButton() {
        setSupportActionBar(binding.header.toolbar)
        supportActionBar.apply {
            if (this != null) {
                setDisplayHomeAsUpEnabled(true)
                setDisplayShowHomeEnabled(false)
                title = ""
                setHomeAsUpIndicator(R.drawable.md_nav_back)
                binding.header.toolbar.setNavigationOnClickListener {
                    super.onBackPressed()
                }
            }
        }
    }

    private fun handle(state: MainViewState) {
        when {
            state.isLoading -> progressDialog.showLoadingDialog(this, null)
            state.isSuccess -> {
                when (state.responseType) {
                    BaseUseCase.ResponseType.DELETE_LOCAL_DATABASE -> {
                        progressDialog.dismissLoadingDialog()
                        Firebase.auth.signOut()
                        logout()
                    }
                    BaseUseCase.ResponseType.SWITCH_ACCOUNT -> {
                        progressDialog.dismissLoadingDialog()
                        finish()
                        overridePendingTransition(0, 0)
                        startActivity(intent)
                        overridePendingTransition(0, 0)
                    }
                    else -> {}
                }
            }
            state.uiError != null -> {
                progressDialog.dismissLoadingDialog()
                showErrorMessage(state.uiError)
            }
            else -> {
                progressDialog.dismissLoadingDialog()
                when (state.mainNavigation) {
                    Welcome -> navigate(Paths.URI_WELCOME)
                    Login -> navigate(Paths.URI_LOGIN)
                    Register -> navigate(Paths.URI_REGISTER)
                    Home -> {
                        if (preferenceManager.accountType == UserType.USER_TYPE_INDIVIDUAL.type) {
                            checkPermission(Paths.URI_SCAN)
                        } else if (preferenceManager.accountType == UserType.USER_TYPE_MERCHANT.type) {
                            navigate(Paths.URI_SELL)
                        }
                    }
                    Sale -> navigate(Paths.URI_SALES)
                    Scan -> navigate(Paths.URI_SCAN)
                    Authorise -> navigate(Paths.URI_AUTHORISE)
                    MyQr -> navigate(Paths.URI_MY_QR)
                    Sell -> navigate(Paths.URI_SELL)
                    GenerateQr -> navigate(Paths.URI_GENERATE)
                    ForgotPassword -> navigate(Paths.URI_FORGOT)
                    else -> {}
                }
            }
        }
    }

    private fun showErrorMessage(error: MainViewError) {
        val message = when (error) {
            TokenExpire -> getString(R.string.title_session_expire)
            NetworkError -> getString(R.string.network_connection)
            else -> getString(R.string.unknown_error)
        }
        val dialog = AlertDialogs(
            this,
            getString(R.string.dialog_info),
            message,
            isPositiveButton = true,
            isCancelButton = false,
            positive_button_text = getString(R.string.btn_ok),
            listener = object : AlertDialogsListener {

                override fun onClickClose() {
                }

                override fun onClickPositive() {
                    if (error == TokenExpire) {
                        logout()
                    }
                }
            }
        )
        dialog.show()
    }

    private fun logout() {
        preferenceManager.removeLoginUser()
        navigate(Paths.URI_WELCOME)
        closeDrawer()
    }

    private fun closeDrawer() {
        binding.drawerLayout.closeDrawer(GravityCompat.START)
    }

    private fun initializeList() {
        list.clear()
        list.add(
            NavigationDrawerModel(
                R.string.nav_home,
                R.drawable.ic_home,
                false,
                NavigationType.TYPE_ITEMS
            )
        )

        /*  list.add(
              NavigationDrawerModel(
                  R.string.nav_scan_and_buy,
                  R.drawable.ic_scan,
                  false,
                  NavigationType.TYPE_ITEMS
              )
          )

          list.add(
              NavigationDrawerModel(
                  R.string.nav_show_my_qr,
                  R.drawable.ic_qr_code,
                  false,
                  NavigationType.TYPE_ITEMS
              )
          )*/

        list.add(
            NavigationDrawerModel(
                R.string.transaction_history,
                R.drawable.ic_invoice_history,
                false,
                NavigationType.TYPE_ITEMS
            )
        )

        if (preferenceManager.accountType == UserType.USER_TYPE_MERCHANT.type) {
            list.add(
                NavigationDrawerModel(
                    R.string.products,
                    R.drawable.ic_product,
                    false,
                    NavigationType.TYPE_ITEMS
                )
            )
        }

        /*  list.add(
              NavigationDrawerModel(
                  R.string.nav_generate_qr,
                  R.drawable.ic_qr_code,
                  false,
                  NavigationType.TYPE_ITEMS
              )
          )*/

        list.add(
            NavigationDrawerModel(
                R.string.nav_logout,
                R.drawable.ic_logout,
                false,
                NavigationType.TYPE_ITEMS
            )
        )

        setRecyclerView()
        setUserProfile()
    }

    private fun setRecyclerView() {
        adapter = NavigationDrawerAdapter(list, this, this)
        binding.rvDrawer.adapter = adapter
    }

    private fun setUserProfile() {
        bindingProfile.itemName.text = preferenceManager.name
        toggleSelection()
    }

    override fun onDrawerItemClick(position: Int, item: NavigationDrawerModel) {
        when (item.stringResourceId) {
            R.string.nav_home -> {
                if (preferenceManager.accountType == UserType.USER_TYPE_INDIVIDUAL.type) {
                    checkCameraPermission(Paths.URI_SCAN)

                } else if (preferenceManager.accountType == UserType.USER_TYPE_MERCHANT.type) {
                    navigate(Paths.URI_SELL)
                }
                toolbarStatus(View.VISIBLE, DrawerLayout.LOCK_MODE_UNLOCKED)
                actionBar()
                closeDrawer()
            }

            R.string.transaction_history -> {
                navigate(Paths.URI_SALES)
                toolbarStatus(View.VISIBLE, DrawerLayout.LOCK_MODE_UNLOCKED)
                actionBar()
                closeDrawer()
            }

            R.string.products -> {
                navigate(Paths.URI_PRODUCTS)
                toolbarStatus(View.VISIBLE, DrawerLayout.LOCK_MODE_UNLOCKED)
                actionBar()
                closeDrawer()
            }

            /*   R.string.nav_scan_and_buy -> {
                   checkCameraPermission(Paths.URI_SCAN)
               }

               R.string.nav_show_my_qr -> {
                   navigate(Paths.URI_MY_QR)
                   toolbarStatus(View.VISIBLE, DrawerLayout.LOCK_MODE_UNLOCKED)
                   actionBar()
                   closeDrawer()
               }

               R.string.nav_generate_qr -> {
                   navigate(Paths.URI_SELL)
                   toolbarStatus(View.VISIBLE, DrawerLayout.LOCK_MODE_UNLOCKED)
                   actionBar()
                   closeDrawer()
               }*/

            R.string.nav_logout -> {
                val dialog = AlertDialogs(
                    this,
                    getString(R.string.nav_logout),
                    getString(R.string.are_you_sure_logout_your_device),
                    isPositiveButton = true,
                    isCancelButton = true,
                    positive_button_text = getString(R.string.yes),
                    cancel_button_text = getString(R.string.cancel),
                    listener = object : AlertDialogsListener {

                        override fun onClickClose() {
                        }

                        override fun onClickPositive() {
                            viewModel.logOut()
                        }
                    }
                )
                dialog.show()
            }
            else -> {
                toolbarStatus(View.VISIBLE, DrawerLayout.LOCK_MODE_UNLOCKED)
            }
        }
    }

    private fun actionBar() {
        setSupportActionBar(binding.header.toolbar)
        actionBarDrawerToggle = object : ActionBarDrawerToggle(
            this, binding.drawerLayout,
            binding.header.toolbar, R.string.nav_open, R.string.nav_close
        ) {
            /** Called when a drawer has settled in a completely closed state.  */
            override fun onDrawerClosed(view: View) {
                super.onDrawerClosed(view)
            }

            /** Called when a drawer has settled in a completely open state.  */
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                binding.nestedScrollView.scrollTo(0, 0)
                binding.rvDrawer.smoothScrollToPosition(0)
            }
        }
        binding.drawerLayout.addDrawerListener(actionBarDrawerToggle as ActionBarDrawerToggle)
        actionBarDrawerToggle?.isDrawerIndicatorEnabled = true
        actionBarDrawerToggle?.syncState()
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun navigate(uri: Uri) {
        navController.navigate(
            uri, NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setPopUpTo(R.id.nav_graph, true)
                .build()
        )
    }

    private fun navigation(uri: Uri) {
        navController.navigate(uri,
            navOptions {
                anim {
                    exit = R.anim.fade_out
                    enter = R.anim.slide_in_v_pop
                    popEnter = R.anim.fade_in
                    popExit = R.anim.slide_out_v
                }
            })
    }

    private fun navigate(id: Int, bundle: Bundle) {
        navController.navigate(id, bundle)
    }

    override fun onNavigateUp(): Boolean {
        return navController.navigateUp()
    }

    private fun checkCameraPermission(uri: Uri) {
        if (permissionManager.canTakeCameraPhoto) {
            navigate(uri)
            toolbarStatus(View.VISIBLE, DrawerLayout.LOCK_MODE_UNLOCKED)
            actionBar()
            closeDrawer()
        } else {
            this.uri = uri
            cameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun checkPermission(uri: Uri) {
        if (permissionManager.canTakeCameraPhoto) {
            navigate(uri)
        } else {
            this.uri = uri
            cameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun hideKeyBoard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun logoutUser(status: Boolean) {
        if (status) {
            viewModel.logOut()
        }
    }

    override fun userUpdate(status: Boolean) {
        setUserProfile()
    }
}