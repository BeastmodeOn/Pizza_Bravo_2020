package com.example.pizzabravo2020.Interface;

import androidx.recyclerview.widget.RecyclerView;

public interface RecylerItemTouchHelperListener {

    void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position);
}
