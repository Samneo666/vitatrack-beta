// SweetAlert2 全站預設設定
// 統一套用視窗寬度（400px）與字體大小（15px）
window.Swal = Swal.mixin({
    width: 400,
    didOpen: function (popup) {
        popup.style.fontSize = '13px';
    }
});
