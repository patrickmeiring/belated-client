package qut.belated.depositing;

import java.util.ArrayList;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class DepositedFileAdapter extends BaseAdapter {

	List<DepositedFileRow> rows;
	
	public DepositedFileAdapter(LayoutInflater inflator, Iterable<DepositedFile> files)
	{
		this.rows = new ArrayList<DepositedFileRow>();
		
		for (DepositedFile file : files)
		{
			rows.add(new DepositedFileRow(inflator, file));
		}
	}
	
	@Override
	public int getCount() {
		return rows.size();
	}

	@Override
	public Object getItem(int index) {
		return rows.get(index).getValue();
	}

	@Override
	public long getItemId(int index) {
		return index;
	}

	@Override
	public View getView(int index, View recycleView, ViewGroup parent) {
		return rows.get(index).getView(recycleView);
	}

}
