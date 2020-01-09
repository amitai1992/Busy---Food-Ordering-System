package com.example.busy.users;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.busy.R;
import com.example.busy.restaurant.Restaurant_page;
import com.example.busy.restaurant.Rforms.Restaurant_Form;
import com.example.busy.users.Uform.Users_Form;
import com.example.busy.users.Uform.filter_form;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Home_users extends AppCompatActivity implements View.OnClickListener {
    private Users_Form u;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();//the current online user
    private DatabaseReference ref_users; //the reference for Users realtimedatabase
    private DatabaseReference ref_rests; //the reference for Restaurant realtimedatabase
    private ListView listView;
    private ArrayList<Restaurant_Form> rest_f = new ArrayList<>();
    private ArrayList<String> rest_list = new ArrayList<>(); //will contains the data of all the restourounts
    private ArrayAdapter<String> rest_adapter; //the addapter that will get the rest_list and will be added to the list view

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_users);

        //INIT
        findViewById(R.id.filter_btn).setOnClickListener(this);
        findViewById(R.id.personal).setOnClickListener(this); //click listener 0f personal settings
        listView = (ListView) findViewById(R.id.rest_view);
        ref_users = FirebaseDatabase.getInstance().getReference("Users"); //get reference to Users
        ref_rests = FirebaseDatabase.getInstance().getReference("Restaurant"); //get reference to Restaurant


        // show the name of the user on top of the page
        ref_users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                u = dataSnapshot.child(user.getUid()).getValue(Users_Form.class); // get user id of the current user
                TextView Hello_Name = findViewById(R.id.hello_name);
                Hello_Name.setText("Hello, " + u.getFirstName());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Home_users.this, "" + databaseError.toString(), Toast.LENGTH_LONG).show();
            }
        });

        this.getData();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent i_restpage = new Intent(Home_users.this, Restaurant_page.class);
                //
                i_restpage.putExtra("rest_uid",rest_f.get(i).getUID());
                startActivity(i_restpage);
            }
        });
    }

    public void getData() {

        Intent i = getIntent();
        final filter_form fm =(filter_form) i.getSerializableExtra("filter");

        Query query = ref_rests.orderByChild("location").equalTo(fm.getCity());// order the database by location
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!rest_list.isEmpty()) {
                    rest_list.clear();
                    rest_f.clear();
                    rest_adapter.clear();
                    listView.clearAnimation();
                }

                if (dataSnapshot.exists()) { //if there is a restourants in this area
                    String rest_string, rest_kosher, rest_type;
                    for (DataSnapshot db : dataSnapshot.getChildren()) {
                        //getting the current restaurant info
                        rest_kosher = db.child("kosher").getValue(String.class);
                        rest_type = db.child("type").getValue(String.class);

                        //checking if the info of the current restaurant is matching to the filter info
                        if (rest_kosher.equals(fm.getKosher()) && rest_type.equals(fm.getType())) {
                            rest_string = db.child("name").getValue(String.class); //get name of the restourants
                            Restaurant_Form temp_rest = db.getValue(Restaurant_Form.class);
                            rest_f.add(temp_rest);
                            rest_list.add(rest_string);
                        } else if (fm.getType().isEmpty() && fm.getKosher().isEmpty()) {
                            rest_string = db.child("name").getValue(String.class); //get name of the restourants
                            Restaurant_Form temp_rest = db.getValue(Restaurant_Form.class);
                            rest_f.add(temp_rest);
                            rest_list.add(rest_string);
                        } else if (rest_kosher.equals(fm.getKosher()) && fm.getType().isEmpty()) {
                            rest_string = db.child("name").getValue(String.class); //get name of the restourants
                            Restaurant_Form temp_rest = db.getValue(Restaurant_Form.class);
                            rest_f.add(temp_rest);
                            rest_list.add(rest_string);
                        } else if (rest_type.equals(fm.getType()) && fm.getKosher().isEmpty()){
                            rest_string = db.child("name").getValue(String.class); //get name of the restourants
                            Restaurant_Form temp_rest = db.getValue(Restaurant_Form.class);
                            rest_f.add(temp_rest);
                            rest_list.add(rest_string);
                        }
                    }
                    rest_adapter = new ArrayAdapter<String>(Home_users.this, R.layout.cutsumefont, rest_list);
                    listView.setAdapter(rest_adapter);


                } else {
                    Toast.makeText(Home_users.this, "No Restaurant in this location! ", Toast.LENGTH_LONG).show();
                    rest_list.add("no restaurants here");
                    rest_adapter = new ArrayAdapter<String>(Home_users.this, R.layout.cutsumefont, rest_list);
                    listView.setAdapter(rest_adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Home_users.this, "cant show resturuonts", Toast.LENGTH_LONG).show();

            }
        });
    }

    //click functoin on personal settings button
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.personal:
                Intent i = new Intent(Home_users.this, personal_settings.class);
                startActivity(i);
                break;

            case R.id.filter_btn:
                Intent j = new Intent(Home_users.this, filter_u.class);
                startActivity(j);
                break;
        }
    }

}
