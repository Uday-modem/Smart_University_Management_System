import React, { useState, useEffect, useContext, useMemo } from 'react';
import axios from 'axios';
import { AuthContext } from '../context/AuthContext';
import './StaffAttendance.css';

const API_BASE_URL = 'http://localhost:8080/api';

const MONTHS = [
	{ value: 1, name: 'January' },
	{ value: 2, name: 'February' },
	{ value: 3, name: 'March' },
	{ value: 4, name: 'April' },
	{ value: 5, name: 'May' },
	{ value: 6, name: 'June' },
	{ value: 7, name: 'July' },
	{ value: 8, name: 'August' },
	{ value: 9, name: 'September' },
	{ value: 10, name: 'October' },
	{ value: 11, name: 'November' },
	{ value: 12, name: 'December' }
];

const StaffAttendance = () => {
	const { token } = useContext(AuthContext);

	// ==================== STATE ====================
	const [branches, setBranches] = useState([]);
	const [selectedBranch, setSelectedBranch] = useState('');
	const [selectedMonth, setSelectedMonth] = useState('');
	const [currentYear] = useState(new Date().getFullYear());
	const [attendanceData, setAttendanceData] = useState(null);
	const [loading, setLoading] = useState(false);
	const [error, setError] = useState('');
	const [debugInfo, setDebugInfo] = useState('');

	// ==================== AXIOS SETUP ====================
	const authAxios = useMemo(() => {
		console.log('🔐 Creating authAxios with token:', token ? '✅ Present' : '❌ Missing');
		return axios.create({
			baseURL: API_BASE_URL,
			headers: {
				Authorization: `Bearer ${token}`,
				'Content-Type': 'application/json'
			}
		});
	}, [token]);

	// ==================== FETCH BRANCHES ====================
	useEffect(() => {
		const fetchBranches = async () => {
			console.log('📡 [EFFECT] Fetching branches...');
			try {
				const response = await authAxios.get('/sections/branches');
				console.log('✅ [SUCCESS] Branches loaded:', response.data);
				setBranches(response.data || []);
				setDebugInfo(`✅ Branches loaded: ${response.data?.length || 0} items`);
			} catch (err) {
				console.error('❌ [ERROR] Failed to load branches:', err.response?.data || err.message);
				setError('Failed to load branches');
				setDebugInfo(`❌ Error: ${err.response?.status} - ${err.message}`);
			}
		};

		if (token) {
			fetchBranches();
		} else {
			console.warn('⚠️ [WARNING] No token available, skipping branch fetch');
		}
	}, [authAxios, token]);

	// ==================== FETCH ATTENDANCE ====================
	useEffect(() => {
		if (selectedBranch && selectedMonth && token) {
			const fetchAttendance = async () => {
				console.log(
					'📡 [EFFECT] Fetching attendance for:',
					{
						branch: selectedBranch,
						month: selectedMonth,
						year: currentYear
					}
				);

				setLoading(true);
				setError('');
				setDebugInfo('🔄 Loading attendance data...');

				try {
					const endpoint = `/attendance/view/staff/${encodeURIComponent(
						selectedBranch
					)}/${currentYear}/${selectedMonth}`;

					console.log('🌐 [REQUEST] Endpoint:', `${API_BASE_URL}${endpoint}`);
					console.log('🔑 [REQUEST] Token:', token ? '✅ Present' : '❌ Missing');

					const response = await authAxios.get(endpoint);

					console.log('📦 [RESPONSE] Raw data:', response.data);
					console.log('📊 [RESPONSE] Status:', response.status);

					// ==================== DATA PROCESSING ====================
					if (response.data && response.data.staff) {
						console.log(`📋 Processing ${response.data.staff.length} staff members`);

						const staffWithCounts = response.data.staff.map((staff, index) => {
							let presentCount = 0;
							let lateCount = 0;
							let absentCount = 0;

							// Count from attendance map
							// Backend returns: { "2025-12-01": "PRESENT", "2025-12-02": "ABSENT", ... }
							if (staff.attendance && typeof staff.attendance === 'object') {
								Object.entries(staff.attendance).forEach(([date, status]) => {
									console.log(
										`  Staff ${index + 1} - ${date}: ${status}`
									);

									if (status === 'PRESENT') presentCount++;
									else if (status === 'LATE') lateCount++;
									else if (status === 'ABSENT') absentCount++;
								});
							}

							console.log(
								`✅ Staff ${index + 1} (${staff.name}): P=${presentCount}, L=${lateCount}, A=${absentCount}`
							);

							return {
								...staff,
								presentCount,
								lateCount,
								absentCount
							};
						});

						const processedData = {
							...response.data,
							staff: staffWithCounts
						};

						console.log('✅ [PROCESSED] Final data:', processedData);
						setAttendanceData(processedData);
						setDebugInfo(
							`✅ Loaded ${staffWithCounts.length} staff with ${response.data.totalDays || 'unknown'} working days`
						);
					} else {
						console.warn('⚠️ [WARNING] Invalid response structure:', response.data);
						setAttendanceData(response.data);
						setDebugInfo('⚠️ Response structure unexpected');
					}
				} catch (err) {
					console.error('❌ [ERROR] Attendance fetch failed:', {
						status: err.response?.status,
						data: err.response?.data,
						message: err.message
					});

					let errorMsg = 'Failed to load attendance data';

					if (err.response?.status === 404) {
						errorMsg = `❌ Endpoint not found - Check backend route: /attendance/view/staff/{branch}/{year}/{month}`;
					} else if (err.response?.status === 401) {
						errorMsg = '❌ Unauthorized - Check authentication token';
					} else if (err.response?.status === 500) {
						errorMsg = `❌ Server error - Check backend logs`;
					} else if (err.response?.status === 403) {
						errorMsg = '❌ Forbidden - Check permissions';
					}

					setError(errorMsg);
					setDebugInfo(`❌ ${err.response?.status || 'Network'} Error: ${err.message}`);
					setAttendanceData(null);
				} finally {
					setLoading(false);
				}
			};

			fetchAttendance();
		} else {
			console.log('⏭️ [SKIP] Waiting for branch/month selection');
		}
	}, [selectedBranch, selectedMonth, currentYear, authAxios, token]);

	// ==================== EXPORT EXCEL ====================
	const handleExportExcel = async () => {
		console.log('📥 [ACTION] Exporting Excel...');

		try {
			const monthName =
				MONTHS.find((m) => m.value === parseInt(selectedMonth))?.name || selectedMonth;
			const endpoint = `/attendance/download/staff/${encodeURIComponent(
				selectedBranch
			)}/${currentYear}/${selectedMonth}`;

			console.log('🌐 [REQUEST] Export endpoint:', `${API_BASE_URL}${endpoint}`);

			const response = await authAxios.get(endpoint, { responseType: 'blob' });

			console.log('✅ [SUCCESS] Excel file received, size:', response.data.size, 'bytes');

			const url = window.URL.createObjectURL(new Blob([response.data]));
			const link = document.createElement('a');
			link.href = url;
			const filename = `staff_attendance_${selectedBranch}_${monthName}_${currentYear}.xlsx`;
			link.setAttribute('download', filename);

			console.log('💾 [SAVE] Downloading file:', filename);

			document.body.appendChild(link);
			link.click();
			link.remove();
			window.URL.revokeObjectURL(url);

			setDebugInfo(`✅ Exported: ${filename}`);
		} catch (err) {
			console.error('❌ [ERROR] Excel export failed:', err.message);
			setError('Failed to export Excel file');
			setDebugInfo(`❌ Export failed: ${err.message}`);
		}
	};

	// ==================== BRANCH CHANGE HANDLER ====================
	const handleBranchChange = (e) => {
		const branch = e.target.value;
		console.log('🔄 [ACTION] Branch changed to:', branch);
		setSelectedBranch(branch);
		setSelectedMonth('');
		setAttendanceData(null);
		setError('');
		setDebugInfo('');
	};

	// ==================== MONTH CHANGE HANDLER ====================
	const handleMonthChange = (e) => {
		const month = e.target.value;
		console.log('🔄 [ACTION] Month changed to:', month);
		setSelectedMonth(month);
		setError('');
	};

	// ==================== RENDER ====================
	return (
		<div className="staff-attendance-container">
			{/* ==================== HEADER ==================== */}
			<div className="staff-attendance-header">
				<h1>📊 Staff Attendance Management</h1>
				<p>View and manage staff attendance records by branch and month</p>
			</div>

			{/* ==================== DEBUG INFO ==================== */}
			{debugInfo && (
				<div className="debug-info" style={{ padding: '10px', backgroundColor: '#f0f0f0', borderRadius: '5px', marginBottom: '15px', fontSize: '12px' }}>
					{debugInfo}
				</div>
			)}

			{/* ==================== ERROR MESSAGE ==================== */}
			{error && (
				<div className="error-message" style={{ padding: '15px', backgroundColor: '#fee', color: '#c00', borderRadius: '5px', marginBottom: '15px', border: '1px solid #fcc' }}>
					⚠️ {error}
				</div>
			)}

			{/* ==================== FILTERS ==================== */}
			<div className="filters-section">
				<div className="filter-group">
					<label htmlFor="branch-select">Select Branch:</label>
					<select
						id="branch-select"
						value={selectedBranch}
						onChange={handleBranchChange}
						className="filter-select"
					>
						<option value="">-- Choose Branch --</option>
						{branches.length > 0 ? (
							branches.map((branch) => (
								<option key={branch} value={branch}>
									{branch}
								</option>
							))
						) : (
							<option disabled>Loading branches...</option>
						)}
					</select>
				</div>

				<div className="filter-group">
					<label htmlFor="month-select">Select Month:</label>
					<select
						id="month-select"
						value={selectedMonth}
						onChange={handleMonthChange}
						disabled={!selectedBranch}
						className="filter-select"
					>
						<option value="">-- Choose Month --</option>
						{MONTHS.map((month) => (
							<option key={month.value} value={month.value}>
								{month.name}
							</option>
						))}
					</select>
				</div>

				{selectedBranch && selectedMonth && (
					<button onClick={handleExportExcel} className="export-btn">
						📥 Export to Excel
					</button>
				)}
			</div>

			{/* ==================== LOADING STATE ==================== */}
			{loading && (
				<div className="loading" style={{ padding: '20px', textAlign: 'center', fontSize: '16px', color: '#666' }}>
					⏳ Loading attendance data for {selectedBranch} - {MONTHS.find(m => m.value === parseInt(selectedMonth))?.name}...
				</div>
			)}

			{/* ==================== ATTENDANCE TABLE ==================== */}
			{attendanceData && attendanceData.staff && attendanceData.staff.length > 0 ? (
				<div className="table-container">
					<div className="table-header">
						<h3>
							Attendance Report - {selectedBranch} ({MONTHS.find(m => m.value === parseInt(selectedMonth))?.name} {currentYear})
						</h3>
						<p>Total Staff: {attendanceData.staff.length} | Working Days: {attendanceData.totalDays}</p>
					</div>

					<table className="attendance-table">
						<thead>
							<tr>
								<th>Staff Name</th>
								<th>Staff ID</th>
								{/* Generate columns for each day of the month */}
								{attendanceData.totalDays &&
									Array.from({ length: attendanceData.totalDays }, (_, i) => (
										<th key={`day-${i + 1}`} className="day-header">
											{i + 1}
										</th>
									))}
								<th className="summary-header present-header">P</th>
								<th className="summary-header late-header">L</th>
								<th className="summary-header absent-header">A</th>
							</tr>
						</thead>
						<tbody>
							{attendanceData.staff.map((staff, rowIndex) => (
								<tr key={staff.id || rowIndex} className="staff-row">
									<td className="staff-name">{staff.name}</td>
									<td className="staff-id">{staff.staffId}</td>

									{/* Generate attendance cells for each day */}
									{attendanceData.totalDays &&
										Array.from({ length: attendanceData.totalDays }, (_, i) => {
											const day = i + 1;
											// Construct the date string in YYYY-MM-DD format
											const dateStr = `${currentYear}-${String(selectedMonth).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
											// Get the status from the attendance map
											const status = staff.attendance && staff.attendance[dateStr];

											return (
												<td
													key={`${staff.id}-${day}`}
													className={`attendance-cell ${status ? status.toLowerCase() : 'unmarked'}`}
													title={
														status
															? `${dateStr}: ${status}`
															: `${dateStr}: Not Marked`
													}
												>
													{status === 'PRESENT'
														? 'P'
														: status === 'ABSENT'
														? 'A'
														: status === 'LATE'
														? 'L'
														: '-'}
												</td>
											);
										})}

									<td className="summary-cell present">
										<strong>{staff.presentCount || 0}</strong>
									</td>
									<td className="summary-cell late">
										<strong>{staff.lateCount || 0}</strong>
									</td>
									<td className="summary-cell absent">
										<strong>{staff.absentCount || 0}</strong>
									</td>
								</tr>
							))}
						</tbody>
					</table>

					{/* ==================== SUMMARY STATISTICS ==================== */}
					<div className="summary-section">
						<h3>📈 Month Summary</h3>
						<div className="summary-stats">
							<div className="stat-card">
								<div className="stat-label">Total Staff</div>
								<div className="stat-value">{attendanceData.staff.length}</div>
							</div>
							<div className="stat-card">
								<div className="stat-label">Total Working Days</div>
								<div className="stat-value">{attendanceData.totalDays}</div>
							</div>
							<div className="stat-card">
								<div className="stat-label">Month & Year</div>
								<div className="stat-value">
									{MONTHS.find((m) => m.value === parseInt(selectedMonth))?.name} {currentYear}
								</div>
							</div>
							<div className="stat-card">
								<div className="stat-label">Branch</div>
								<div className="stat-value">{selectedBranch}</div>
							</div>
						</div>
					</div>
				</div>
			) : !loading && selectedBranch && selectedMonth ? (
				<div className="no-data-message" style={{ padding: '30px', textAlign: 'center', backgroundColor: '#f9f9f9', borderRadius: '5px' }}>
					📭 No staff found for {selectedBranch} in {MONTHS.find(m => m.value === parseInt(selectedMonth))?.name}
				</div>
			) : !loading && !selectedBranch ? (
				<div className="placeholder-message" style={{ padding: '30px', textAlign: 'center', color: '#999' }}>
					👆 Please select a branch to get started
				</div>
			) : null}
		</div>
	);
};

export default StaffAttendance;