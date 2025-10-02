using HabitAqui.Data;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

public class RoleManagerController : Controller
{
    private readonly RoleManager<IdentityRole> _roleManager;
    public RoleManagerController(RoleManager<IdentityRole> roleManager)
    {
        _roleManager = roleManager;
    }

    [Authorize(Roles="Admin")]
    public async Task<IActionResult> Index()
    {
        return View(await _roleManager.Roles.ToListAsync());
    }
    [HttpPost]
    [Authorize(Roles = "Admin")]
    public async Task<IActionResult> AddRole(string roleName)
    {
        /* código a criar */
        return RedirectToAction("Index");
    }
}