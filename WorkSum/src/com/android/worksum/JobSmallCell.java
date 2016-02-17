package com.android.worksum;

import android.widget.ImageView;
import android.widget.TextView;

import com.jobs.lib_v1.list.DataListCell;

public class JobSmallCell extends DataListCell{
	
	private TextView mJobName;
	private TextView mCompanyName;
	private TextView mJobAddress;
	
	private ImageView mJobPicture;
	private TextView mJobSalary;

	@Override
	public void bindData() {
		mJobName.setText(mDetail.getString("JobName"));
		mCompanyName.setText(mDetail.getString("CustomerName"));
		mJobAddress.setText(mDetail.getString("AreaName"));
		mJobSalary.setText(mDetail.getString("Salary"));
		mJobPicture.setImageDrawable(mAdapter.getContext().getResources().getDrawable(R.drawable.ic_launcher));
	}

	@Override
	public void bindView() {
		mJobName = (TextView) findViewById(R.id.job_name);
		mCompanyName = (TextView) findViewById(R.id.company_name);
		mJobAddress = (TextView) findViewById(R.id.job_address);
		mJobPicture = (ImageView) findViewById(R.id.job_pic);
		mJobSalary = (TextView) findViewById(R.id.job_salary);
	}

	@Override
	public int getCellViewLayoutID() {
		return R.layout.job_small_cell;
	}

}
