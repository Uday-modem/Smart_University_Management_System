// src/utils/branchMapping.js

export const BRANCH_NAMES = {
  'ECE': 'Electronics and Communication Engineering',
  'CSE': 'Computer Science and Engineering',
  'EEE': 'Electrical and Electronics Engineering',
  'MECH': 'Mechanical Engineering',
  'CIVIL': 'Civil Engineering',
  'IT': 'Information Technology',
  'AIDS': 'Artificial Intelligence and Data Science',
  'AIML': 'Artificial Intelligence and Machine Learning',
  'CSM': 'Computer Science and Engineering (AI & ML)',
  'CSD': 'Computer Science and Engineering (Data Science)',
  'IOT': 'Internet of Things',
  'CS': 'Computer Science',
  'ME': 'Mechanical Engineering',
  'CE': 'Civil Engineering'
};

// Get full name from code
export const getBranchName = (code) => {
  return BRANCH_NAMES[code] || code; // Fallback to code if not found
};

// Get code from full name (reverse lookup)
export const getBranchCode = (fullName) => {
  const entry = Object.entries(BRANCH_NAMES).find(([code, name]) => name === fullName);
  return entry ? entry[0] : fullName;
};

// Get all branches as array of objects for dropdown
export const getBranchesForDropdown = (branchCodes) => {
  return branchCodes.map(code => ({
    code: code,
    name: BRANCH_NAMES[code] || code
  }));
};
