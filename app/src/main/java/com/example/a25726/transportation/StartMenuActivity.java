package com.example.a25726.transportation;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class StartMenuActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mEtStart,mEtDestination;
    private Button mBtToMap;
    public String start,destination;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_menu);

        mEtStart = findViewById(R.id.et_start);
        mEtDestination = findViewById(R.id.et_destination);
        mBtToMap = findViewById(R.id.bt_toMap);

        mBtToMap.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.bt_toMap){
            Intent intent = new Intent(StartMenuActivity.this,MainActivity.class);
            Bundle b =new Bundle();
            start = mEtStart.getText().toString();
            destination = mEtDestination.getText().toString();
            if(start.equals("")||destination.equals("")){
                Toast toast = Toast.makeText(StartMenuActivity.this,"信息输入不完整！",Toast.LENGTH_SHORT);
                toast.show();
            }else{
                b.putString("start",start);
                b.putString("destination",destination);
                intent.putExtras(b);
                startActivity(intent);
            }

        }
    }
}
