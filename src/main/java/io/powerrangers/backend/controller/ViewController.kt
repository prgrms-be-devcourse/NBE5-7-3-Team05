package io.powerrangers.backend.controller

import jakarta.servlet.http.HttpSession
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class ViewController {
    @GetMapping("/loginPage")
    fun loginPage(session: HttpSession, model: Model): String {
        val errorMessage = session.getAttribute("error") as? String // String이면 String으로, 아니면 Null로
        errorMessage?.let {
            model.addAttribute("error", it)
            session.removeAttribute("error")
        }
        return "loginpage"
    }

    // 🟢 마이페이지 조회 (view: mypage.html)
    @GetMapping("/mypage")
    fun showMyPage() = "mypage"

    // 🟢 마이페이지 수정 폼 (view: updatemypage.html)
    @GetMapping("/mypage/update")
    fun showUpdateMyPage() = "updatemypage"
}
