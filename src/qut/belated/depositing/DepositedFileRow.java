package qut.belated.depositing;

import qut.belated.R;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DepositedFileRow {
	
	private DepositedFile value;
	private LayoutInflater inflater;
	
	public DepositedFileRow(LayoutInflater inflater, DepositedFile value)
	{
		this.inflater = inflater;
		this.value = value;
	}
	
	public DepositedFile getValue()
	{
		return value;
	}
	
	public View getView(View recycleView)
	{
		View view = recycleView;
		
		if (view == null)
			view = createView();
		
		updateView(view);
		
		return view;
	}
	
	private View createView()
	{
		ViewGroup viewGroup = (ViewGroup)inflater.inflate(R.layout.file_row, null);
		ViewHolder holder = new ViewHolder((TextView)viewGroup.findViewById(R.id.fileName),
				(TextView)viewGroup.findViewById(R.id.fileDescription));
		
		viewGroup.setTag(holder);
		
		return viewGroup;
	}
	
	private void updateView(View view)
	{
		ViewHolder holder = (ViewHolder)view.getTag();
		holder.nameView.setText(value.getName());
		holder.descriptionView.setText(value.getDescription());
	}
	
	
	private class ViewHolder {
		public ViewHolder(TextView nameView, TextView descriptionView)
		{
			this.nameView = nameView;
			this.descriptionView = descriptionView;
		}
		
		final TextView nameView;
		final TextView descriptionView;
	}
}
