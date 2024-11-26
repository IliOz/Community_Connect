package com.example.todo_list;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public String nextAssignment;

    private void setNextAssignment(String nextAssignment)
    {
        this.nextAssignment = nextAssignment;
    }

    private String getNextAssignment()
    {
        return this.nextAssignment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView TODO_List = findViewById(R.id.todo);
        EditText edit = findViewById(R.id.inputField);
        Button submit = findViewById(R.id.submit);
        ListView list = findViewById(R.id.todo_list);
        this.nextAssignment = "";

        ArrayList<String> arrayList = new ArrayList<>();
        ArrayList<Item> arrayList1 = new ArrayList<>();
        CustomAdapter customAdapter = new CustomAdapter(MainActivity.this, 0, arrayList1);
        list.setAdapter(customAdapter);


        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Item item = arrayList1.get(position); // Get the Item object
                Toast.makeText(MainActivity.this, item.getText(), Toast.LENGTH_SHORT).show();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getNextAssignment().isEmpty()){
                    arrayList1.add(new Item(getNextAssignment()));
                    customAdapter.notifyDataSetChanged();
                    edit.setText("");
                    setNextAssignment("");
                    Toast.makeText(MainActivity.this, "Task Added", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Enter a new Task", Toast.LENGTH_SHORT).show();
                }
            }
        });



        edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String newText = edit.getText().toString();
                if (newText.isEmpty()) {
                    return;
                }

                for (int index = 0; index < arrayList.size(); index++) {
                    if (arrayList.get(index).equals(newText)){
                        Toast.makeText(MainActivity.this, "Task Already Exists", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                setNextAssignment(newText);
            }
        });

    }
}