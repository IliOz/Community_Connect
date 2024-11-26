package com.example.todo_list;

import android.view.View;
import android.widget.Toast;

public class Item {
    private String text;
    private boolean isChecked;
    private boolean isDeleted;
    private View.OnClickListener onCheckClickListener;

    public View.OnClickListener getOnCheckClickListener() {
        return onCheckClickListener;
    }

    public View.OnClickListener getOnDeleteClickListener() {
        return onDeleteClickListener;
    }

    private View.OnClickListener onDeleteClickListener;
    public Item(String text) {
        this.text = text;
        this.isChecked = false;
        this.isDeleted = false;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isChecked() {
        return this.isChecked;
    }

    public void setChecked(boolean checked) {
        this.isChecked = checked;
    }

    public boolean isDeleted() {
        return this.isDeleted;
    }

    public void setDeleted() {
        this.isDeleted = true;
    }
}