using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using TPV2.Models;
using Microsoft.AspNetCore.Authorization;

namespace TPV2.Controllers
{

    [Authorize(Roles = "Gestor")]
    public class SetRoleFuncionario : Controller
    {

        private readonly RoleManager<IdentityRole> roleManager;
        private readonly UserManager<AplicationUser> userManager;


        public SetRoleFuncionario(RoleManager<IdentityRole> roleManager, UserManager<AplicationUser> userManager)
        {
            this.roleManager = roleManager;
            this.userManager = userManager;
        }

        
        public async Task<IActionResult> SetRoleFuncAsync()
        {
            var model = new List<AtribFuncionario>();
            foreach (var user in userManager.Users)
            {

                if (await userManager.IsInRoleAsync(user,"Cliente"))
                {
                    var userFunc = new AtribFuncionario
                    {
                        UserId = user.Id,
                        UserName = user.UserName
                    };
                    model.Add(userFunc);
                }
            }
            return View(model);
        }

        [HttpPost]
        public async Task<IActionResult> SetRoleFuncForm(string func_esc)
        {
            
            var user = await userManager.FindByIdAsync(func_esc);

            await userManager.RemoveFromRoleAsync(user, "Cliente");
            await userManager.AddToRoleAsync(user, "Funcionario");

            return Redirect("~/Home");
        }



    }
}
