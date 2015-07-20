package lab.star.surf_iot2015;

import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MenuFragment extends Fragment {

    public MenuFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.fragment_menu, container, false);

        if (!(getActivity() instanceof MainDataConsoleActivity)) {
            layout.findViewById(R.id.toSensors).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getActivity()
                            .startActivity(new Intent(getActivity(), MainDataConsoleActivity.class));
                    getFragmentManager().beginTransaction()
                            .remove(MenuFragment.this)
                            .commit();
                }
            });
        }

        if (!(getActivity() instanceof ReminderActivity)){
            layout.findViewById(R.id.toReminders).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getActivity()
                            .startActivity(new Intent(getActivity(), ReminderActivity.class));
                    getFragmentManager().beginTransaction()
                            .remove(MenuFragment.this)
                            .commit();
                }
            });
        }

        return layout;
    }

}
