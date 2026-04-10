<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="zh-TW">
<head>
<meta charset="UTF-8">
<title>商品列表</title>
</head>
<body>


	<h2>商品測試頁</h2>

	<div>
		<span>維他命C（商品ID: 1</span>
		<button class="btn btn-success" onclick="openAddToCart(1)">選購</button>
	</div>

	<!-- Modal HTML æè¢«è¼é²ä¾ -->
	<div id="modalContainer"></div>

	<!-- Bootstrap -->
	<link
		href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css"
		rel="stylesheet">
	<script
		src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>

	<script>
		const contextPath = "${pageContext.request.contextPath}"
	</script>
	<script src="../js/addToCart.js"></script>

</body>




</html>
