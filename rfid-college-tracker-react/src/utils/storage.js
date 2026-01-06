export const setCurrentUser = (user, userType) => {
    sessionStorage.setItem('currentUser', JSON.stringify(user));
    sessionStorage.setItem('userType', userType);
};

export const getCurrentUser = () => {
    const user = sessionStorage.getItem('currentUser');
    return user ? JSON.parse(user) : null;
};

export const getCurrentUserType = () => {
    return sessionStorage.getItem('userType');
};

export const clearSession = () => {
    sessionStorage.removeItem('currentUser');
    sessionStorage.removeItem('userType');
};

export const isAuthenticated = () => {
    return sessionStorage.getItem('currentUser') !== null;
};
