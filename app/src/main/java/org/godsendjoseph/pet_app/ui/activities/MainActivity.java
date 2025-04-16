package org.godsendjoseph.pet_app.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import org.godsendjoseph.pet_app.R;
import org.godsendjoseph.pet_app.auth.AuthManager;
import org.godsendjoseph.pet_app.auth.SessionManager;
import org.godsendjoseph.pet_app.models.User;
import org.godsendjoseph.pet_app.ui.fragments.DashboardFragment;
import org.godsendjoseph.pet_app.ui.fragments.ExpenseFormFragment;
import org.godsendjoseph.pet_app.ui.fragments.ExpenseListFragment;
import org.godsendjoseph.pet_app.ui.fragments.InsightsFragment;
import org.godsendjoseph.pet_app.ui.fragments.SettingsFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

/**
 * Main activity for the application.
 * Contains a navigation drawer and hosts different fragments.
 */

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private FloatingActionButton fabAddExpense;

    private AuthManager authManager;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize managers
        authManager = AuthManager.getInstance(this);
        sessionManager = SessionManager.getInstance(this);

        // Check if user is logged in
        if (!authManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        // Initialize views
        initViews();
        setupNavigation();
        updateUserInfo();

        // Set default fragment
        if (savedInstanceState == null) {
            displayFragment(new DashboardFragment());
            navigationView.setCheckedItem(R.id.nav_dashboard);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check session validity
        if (!sessionManager.isSessionValid()) {
            sessionManager.endSession();
            redirectToLogin();
        } else {
            sessionManager.refreshSession();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        fabAddExpense = findViewById(R.id.fab_add_expense);

        fabAddExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayFragment(new ExpenseFormFragment());
            }
        });
    }

    private void setupNavigation() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    private void updateUserInfo() {
        View headerView = navigationView.getHeaderView(0);
        TextView tvUsername = headerView.findViewById(R.id.tv_nav_username);
        TextView tvEmail = headerView.findViewById(R.id.tv_nav_email);

        User currentUser = authManager.getCurrentUser();
        if (currentUser != null) {
            tvUsername.setText(currentUser.getUsername());
            tvEmail.setText(currentUser.getEmail());
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_dashboard) {
            displayFragment(new DashboardFragment());
        } else if (id == R.id.nav_expenses) {
            displayFragment(new ExpenseListFragment());
        } else if (id == R.id.nav_insights) {
            displayFragment(new InsightsFragment());
        } else if (id == R.id.nav_settings) {
            displayFragment(new SettingsFragment());
        } else if (id == R.id.nav_logout) {
            logout();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            displayFragment(new SettingsFragment());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void displayFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragment);
        ft.commit();
    }

    private void logout() {
        sessionManager.endSession();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}