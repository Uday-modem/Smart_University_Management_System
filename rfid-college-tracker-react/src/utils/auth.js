export const validateEmail = (email) => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
};

export const validatePassword = (password) => {
    return password.length >= 6;
};

export const validateStudentSignup = (formData) => {
    const errors = {};

    if (!formData.name.trim()) {
        errors.name = "Name is required";
    }

    if (!formData.registrationNumber.trim()) {
        errors.registrationNumber = "Registration number is required";
    }

    if (!formData.email.trim()) {
        errors.email = "Email is required";
    } else if (!validateEmail(formData.email)) {
        errors.email = "Invalid email format";
    }

    if (!formData.password) {
        errors.password = "Password is required";
    } else if (!validatePassword(formData.password)) {
        errors.password = "Password must be at least 6 characters";
    }

    if (formData.password !== formData.confirmPassword) {
        errors.confirmPassword = "Passwords do not match";
    }

    return errors;
};

export const validateStudentLogin = (emailOrReg, password) => {
    const errors = {};

    if (!emailOrReg.trim()) {
        errors.emailOrReg = "Email or Registration number is required";
    }

    if (!password) {
        errors.password = "Password is required";
    }

    return errors;
};

export const validateAdminLogin = (email, password) => {
    const errors = {};

    if (!email.trim()) {
        errors.email = "Email is required";
    } else if (!validateEmail(email)) {
        errors.email = "Invalid email format";
    }

    if (!password) {
        errors.password = "Password is required";
    }

    return errors;
};
