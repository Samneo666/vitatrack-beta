document.addEventListener("DOMContentLoaded", function () {

    const profile = document.getElementById('profile');


    profile.addEventListener("click", function (e) {
        e.preventDefault();
        const readonlyFields = document.querySelectorAll('.member-form .readonly');
        fetch('profile')
            .then(result => result.json())
            .then(member => {

                console.log("成功獲取資料：", member);

                readonlyFields[0].textContent = member.name;
                readonlyFields[1].textContent = member.email;
                readonlyFields[2].textContent = member.phone;
                readonlyFields[3].textContent = member.address;
            })

    });

});

