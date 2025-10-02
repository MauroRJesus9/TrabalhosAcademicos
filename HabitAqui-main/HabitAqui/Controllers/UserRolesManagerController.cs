using HabitAqui.Data;
using HabitAqui.Models;
using HabitAqui.ViewModels;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace HabitAqui.Controllers
{
    public class UserRolesManagerController : Controller
    {

        private readonly ApplicationDbContext _context;

        private readonly UserManager<Utilizadores> _userManager;
        private readonly RoleManager<IdentityRole> _roleManager;
        public UserRolesManagerController(UserManager<Utilizadores> userManager,
                                            RoleManager<IdentityRole> roleManager,
                                            ApplicationDbContext context)
        {
            /* código a criar */
            this._userManager = userManager;
            this._roleManager = roleManager;
            _context = context;
        }
        public async Task<IActionResult> Index(int empresaId)
        {
            /* código a criar */

            var users = await _userManager.Users.Include(u => u.Empresa).ToListAsync();
            var userRolesViewModel = new List<UserRolesViewModel>();

            foreach (var u in users)
            {

                // Verificar se o usuário tem a função "Admin"
                var isAdmin = await _userManager.IsInRoleAsync(u, "Admin");

                if (isAdmin)
                {
                    continue;
                }

                var user = new UserRolesViewModel();
                user.UserId = u.Id;
                user.NomeCompleto = u.UserName;
                user.NIF = u.NIF;
                user.Roles = await GetUserRoles(u);
                user.Empresa = u.Empresa;
                user.EmpresaId = empresaId;
                userRolesViewModel.Add(user);

            }

            return View(userRolesViewModel);
        }
        private async Task<List<string>> GetUserRoles(Utilizadores user)
        {
            return new List<string>(await _userManager.GetRolesAsync(user));
        }
        public async Task<IActionResult> Details(string userId, int empresaId)
        {
            /* código a criar */

            var user = await _userManager.FindByIdAsync(userId);
            if (user == null)
            {
                return NotFound();
            }

            ViewBag.UserId = userId;
            ViewBag.UserName = user.UserName;
            ViewBag.EmpresaId = empresaId;

            var model = new List<ManageUserRolesViewModel>();

            foreach (var role in _roleManager.Roles)
            {
              
                if(role.Name != "Admin" && role.Name != "Cliente")
                {
                    var userRolesManager = new ManageUserRolesViewModel()
                    {
                        RoleId = role.Id,
                        RoleName = role.Name,
                    };

                    userRolesManager.Selected = await _userManager.IsInRoleAsync(user, role.Name);
                    model.Add(userRolesManager);
                }
                
            }
            return View(model);
        }

        [HttpPost]
        public async Task<IActionResult> Details(List<ManageUserRolesViewModel> model,
       string userId, int empresaId)
        {
            /* código a criar */

            var user = await _userManager.FindByIdAsync(userId);
            if (user == null)
            {
                return NotFound();
            }

            var roles = await _userManager.GetRolesAsync(user);
            var result = await _userManager.RemoveFromRolesAsync(user, roles);
            if (!result.Succeeded)
            {
                ModelState.AddModelError("", "Cannot remove");
                return View(model);
            }

            result = await _userManager.AddToRolesAsync(user, model.Where(x => x.Selected).Select(x => x.RoleName));

            if (!result.Succeeded)
            {
                ModelState.AddModelError("", "Cannot add");
                return View(model);
            }

            // Verifique se todas as roles estão desmarcadas
            if (model.All(x => !x.Selected))
            {
                // Todas as roles estão desmarcadas, defina EmpresaId e Empresa como null
                user.EmpresaId = null;
                user.Empresa = null;

                await _userManager.UpdateAsync(user);

                return RedirectToAction("Index");
            }

            // Verificar se a empresaId existe na tabela Empresa
            var empresa = await _context.Empresa.FirstOrDefaultAsync(e => e.Id == empresaId);

            if(empresa != null)
            {
                user.EmpresaId = empresa.Id;
                user.Empresa = empresa;
            }

            /*if (empresa == null)
            {
                // Se empresaId não existe, definir EmpresaId e Empresa como null
                user.EmpresaId = null;
                user.Empresa = null;
            }
            else
            {
                user.EmpresaId = empresa.Id;
                user.Empresa = empresa;
            }*/

            await _userManager.UpdateAsync(user);

            return RedirectToAction("Index");
        }
    }
}
