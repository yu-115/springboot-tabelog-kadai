document.addEventListener('DOMContentLoaded', function() {
    document.getElementById('cancelSubscriptionBtn').addEventListener('click', async function(event) {
        event.preventDefault(); // デフォルトのリンク動作を防止

        const paymentMethodId = this.getAttribute('data-paymentmethod-id');
        const subscriptionId = this.getAttribute('data-subscription-id');

        try {
            const response = await fetch('/stripe/cancel-subscription', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ paymentMethodId, subscriptionId })
            });

            if (response.ok) {
                alert('サブスクリプションとカード情報が正常にキャンセルされました');
                window.location.href = '/';
            } else {
                alert('キャンセル中にエラーが発生しました');
                const errorText = await response.text();
                console.error('サーバーエラーメッセージ:', errorText); // サーバーエラーメッセージを出力
            }
        } catch (error) {
            console.error('キャンセル中にエラーが発生しました:', error);
            alert('キャンセル中にエラーが発生しました');
        }
    });
});
