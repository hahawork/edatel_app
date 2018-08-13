package haha.edatelcontrol.clases;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import haha.edatelcontrol.R;
import haha.edatelcontrol.clases.classClientesPDV;
import haha.edatelcontrol.clases.classColorGenerator;
import haha.edatelcontrol.clases.classTextDrawable;

/**
 * Created by User on 29/01/2018.
 */

public class ListViewClienteAdapter extends ArrayAdapter<classClientesPDV> {

    private Activity activity;
    private List<classClientesPDV> friendList;
    private List<classClientesPDV> searchList;

    public ListViewClienteAdapter(Activity context, int resource, List<classClientesPDV> objects) {
        super(context, resource, objects);
        this.activity = context;
        this.friendList = objects;
        this.searchList = new ArrayList<>();
        this.searchList.addAll(friendList);
    }

    @Override
    public int getCount() {
        return friendList.size();
    }

    @Override
    public classClientesPDV getItem(int position) {
        return friendList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            ViewHolder holder;
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            // If holder not exist then locate all view from UI file.
            if (convertView == null) {
                // inflate UI from XML file
                convertView = inflater.inflate(R.layout.item_listview, parent, false);
                // get all UI view
                holder = new ViewHolder(convertView);
                // set tag for holder
                convertView.setTag(holder);
            } else {
                // if holder created, get tag from view
                holder = (ViewHolder) convertView.getTag();
            }


            if (getItem(position).getNombre().length()>0) {

                holder.friendName.setText(getItem(position).getNombre());
                holder.number.setText(getItem(position).getNumero());
                holder.tvId.setText("" + getItem(position).getId());
                //get first letter of each String item
                String firstLetter = String.valueOf(getItem(position).getNombre().charAt(0));
                classColorGenerator generator = classColorGenerator.MATERIAL; // or use DEFAULT
                // generate random color
                int color = generator.getColor(getItem(position));

                classTextDrawable drawable = classTextDrawable.builder()
                        .buildRound(firstLetter, color); // radius in px

                holder.imageView.setImageDrawable(drawable);

                Double saldocredito = new classMetodosGenerales(getContext())
                        .VerificaClienteTieneSaldoPendientesDeAbonar(getItem(position).getId());

                if (saldocredito > 0) {
                    holder.ivIndicaclientedebe.setVisibility(View.VISIBLE);
                } else {
                    holder.ivIndicaclientedebe.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            new classExceptionHandler(getContext()).uncaughtException(e);
        }
        return convertView;
    }

    // Filter method
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        friendList.clear();
        if (charText.length() == 0) {
            friendList.addAll(searchList);
        } else {
            for (classClientesPDV s : searchList) {
                if (s.getNombre().toLowerCase(Locale.getDefault()).contains(charText)) {
                    friendList.add(s);
                }
            }
        }
        notifyDataSetChanged();
    }

    private class ViewHolder {
        private ImageView imageView, ivIndicaclientedebe;
        private TextView friendName, number, tvId;

        public ViewHolder(View v) {
            imageView = (ImageView) v.findViewById(R.id.image_view);
            friendName = (TextView) v.findViewById(R.id.text);
            number = (TextView) v.findViewById(R.id.number);
            tvId = (TextView) v.findViewById(R.id.id);
            ivIndicaclientedebe = (ImageView) v.findViewById(R.id.ivIndicaclienteDebeCredito);
        }
    }

}