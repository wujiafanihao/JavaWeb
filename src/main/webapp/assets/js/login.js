/**
 * 文件: assets/js/login.js
 */
document.addEventListener('DOMContentLoaded', function() {
    // 消息自动隐藏
    const logoutMessageElement = document.getElementById('logoutMessage');
    if (logoutMessageElement) {
        setTimeout(function() {
            logoutMessageElement.style.display = 'none';
        }, 1500); // 1.5 秒
    }

    const errorMessageElement = document.getElementById('errorMessage');
    if (errorMessageElement) {
        setTimeout(function() {
            errorMessageElement.style.display = 'none';
        }, 1500); // 1.5 秒
    }

    // 表单提交时对密码进行哈希
    // 获取表单元素
    const loginForm = document.getElementById('loginForm');
    // 获取用户输入的原始密码字段
    const passwordOriginalField = document.getElementById('passwordInput');
    // 获取用于提交哈希值的隐藏字段
    const passwordHashedField = document.getElementById('passwordHashedInput');

    // 确保所有需要的元素都存在
    if (loginForm && passwordOriginalField && passwordHashedField) {

        // 为表单添加 'submit' 事件监听器
        loginForm.addEventListener('submit', function(event) {
            // 获取用户输入的明文密码
            const plainPassword = passwordOriginalField.value;

            try {
                // 使用 CryptoJS SHA-256 计算哈希值，并转为十六进制字符串
                const hashedPassword = CryptoJS.SHA256(plainPassword).toString(CryptoJS.enc.Hex);

                // 将计算出的哈希值赋给隐藏字段
                passwordHashedField.value = hashedPassword;

                // (可选) 出于安全考虑，可以清空原始密码字段，防止它被意外提交
                // passwordOriginalField.value = "";

                console.log('Password hashed:', hashedPassword); // 调试用

                // 现在允许表单继续正常提交

            } catch (e) {
                // 如果 CryptoJS 库加载失败或出错
                console.error("Hashing failed:", e);
                alert("客户端密码处理失败，请刷新页面重试。");
                // 阻止表单提交
                event.preventDefault();
            }
        });
    } else {
        console.error("Login form elements not found. Hashing will not work.");
    }

});