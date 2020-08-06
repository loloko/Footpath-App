package com.fernando.footpath.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fernando.footpath.R;
import com.fernando.footpath.adapter.TypeAdapter;
import com.fernando.footpath.model.TypeModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class MessageUtil {
    private static final String TAG = "MessageUtil";

    public static void toastMessage(Context context, int stringRef, String text) {
        if (text == null)
            Toast.makeText(context, stringRef, Toast.LENGTH_LONG).show();
        else
            Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    public static void snackBarMessage(View view, int text) {
        Snackbar.make(view, text, Snackbar.LENGTH_LONG).show();

    }

    public static void snackBarMessage(View view, String text) {
        Snackbar.make(view, text, Snackbar.LENGTH_LONG).show();
    }

    public static AlertDialog createLoadingDialog(Activity activity, @StringRes Integer msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_loading, null);

        TextView text = view.findViewById(R.id.tv_dialog_msg);
        text.setText(msg == null ? randomLoadingMessage() : msg);


        builder.setView(view);
        builder.setCancelable(false);

        return builder.create();
    }

    public static void createBasicDialog(Activity activity, int stringRef) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setMessage(stringRef);
        builder.setTitle(R.string.hello);

        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public static void createFinishDialog(final Activity c, int msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle(R.string.hello);
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                c.finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private static int randomLoadingMessage() {
        int[] lines = new int[]{
                R.string.loading_1, R.string.loading_2,
                R.string.loading_4, R.string.loading_4,
                R.string.loading_5, R.string.loading_6,
                R.string.loading_7,};
        return lines[Math.toIntExact(Math.round(Math.random() * (lines.length - 1)))];
    }

    public static void createDialogRecycler(Activity activity, final TextView image, final List<TypeModel> models, final boolean isTrackType) {
        try {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity, R.style.typeDialog);
            View convertView = LayoutInflater.from(activity).inflate(R.layout.dialog_recycler, null);
            RecyclerView rvTypeList = convertView.findViewById(R.id.dialog_recycler_options);


            TypeAdapter adapter = new TypeAdapter(activity, models);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);

            rvTypeList.addItemDecoration(new DividerItemDecoration(rvTypeList.getContext(),
                    DividerItemDecoration.VERTICAL));

            rvTypeList.setLayoutManager(layoutManager);
            rvTypeList.setHasFixedSize(true);
            rvTypeList.setAdapter(adapter);
            alertDialog.setView(convertView);

            final AlertDialog dialog = alertDialog.create();

            rvTypeList.addOnItemTouchListener(new RecyclerItemClickListener(activity, rvTypeList, new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    image.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, models.get(position).getIcon());
                    if (isTrackType)
                        DataSingleton.getInstance().setTrackType(models.get(position).getId());
                    else
                        DataSingleton.getInstance().setActivityType(models.get(position).getId());

                    dialog.dismiss();
                }

                @Override
                public void onLongItemClick(View view, int position) {

                }

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                }
            }));


            dialog.show();


        } catch (Exception e) {
            Log.e(TAG, "createDialogRecycler: " + e.getMessage());
        }
    }
}
