using HabitAqui.Data;
using HabitAqui.Models;
using HabitAqui.ViewModels;
using HabitAqui.ViewModels.Funcionarios;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using System.Data;


namespace HabitAqui.Controllers
{
    [Authorize(Roles = "Admin, Gestor")]

    public class FuncionariosController : Controller{

        private readonly ApplicationDbContext _context;
        private readonly UserManager<Utilizadores> _userManager;
        private readonly RoleManager<IdentityRole> _roleManager;

        public FuncionariosController(ApplicationDbContext context, UserManager<Utilizadores> userManager, RoleManager<IdentityRole> roleManager)
        {
            _context = context;
            _userManager = userManager;
            _roleManager = roleManager;
        }


        //Criar um registo de utilizador, com o perfil «funcionário» ou com perfil «gestor».

        [HttpGet]
        public IActionResult Create() {

            var roles = _roleManager.Roles.Where(r => r.NormalizedName.Equals("Funcionario")).Select(r => r.Name).ToList();
            var roles2 = _roleManager.Roles.Where(r => r.NormalizedName.Equals("Gestor")).Select(r => r.Name).ToList();
            ViewBag.Roles = new SelectList(roles.Concat(roles2));

            return View();
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create(FuncionarioAddViewModel model)
        {
            if (ModelState.IsValid){

                var novoUsuario = new Utilizadores {
                    UserName = model.Email, 
                    Email = model.Email, 
                    Ativo = model.Ativo, 
                    EmpresaId = GetEmpresaId(),
                    EmailConfirmed = true,
                    PhoneNumberConfirmed = true,
                    NomeCompleto = model.Email
                };
                var resultadoRegistro = await _userManager.CreateAsync(novoUsuario, model.Password);

                if (resultadoRegistro.Succeeded) {

                    var roleExist = await _roleManager.RoleExistsAsync(model.Role);
                    if (roleExist)
                        await _userManager.AddToRoleAsync(novoUsuario, model.Role);

                    return RedirectToAction(nameof(Index));
                }
                else
                    TempData["ErroMensagem"] = "Erro ao criar o utilizador.";
            }

            var roles = _roleManager.Roles.Where(r => r.NormalizedName.Equals("Funcionario")).Select(r => r.Name).ToList();
            var roles2 = _roleManager.Roles.Where(r => r.NormalizedName.Equals("Gestor")).Select(r => r.Name).ToList();
            ViewBag.Roles = new SelectList(roles.Concat(roles2));

            return View(model);
        }


        //Listar os registos dos utilizadores; 

        public async Task<IActionResult> Index(){
            var funcionarios = _userManager.Users
                .Where(u => u.EmpresaId == GetEmpresaId())
                .Select(u => new FuncionarioViewModel
                {
                    Id = u.Id,
                    Username = u.UserName,
                    NrArrendamentos = u.Arrendamentos.Count,
                    Ativo = u.Ativo,
                    Role = _userManager.GetRolesAsync(u).Result.FirstOrDefault()
                })
                .ToList();

            return View(funcionarios);
        }


        //Ativar ou inativar um registo de um utilizador; 

        [HttpPost]
        public async Task<IActionResult> AtivarInativar(string userId){


            var gestorId = _userManager.GetUserId(User);

            if (gestorId == userId) {

                TempData["ErroMensagem"] = "Você não pode ativar ou inativar sua própria conta.";

                return RedirectToAction(nameof(Index));

            }

            var user = await _userManager.FindByIdAsync(userId);

            if (user != null){

                // Invertendo o estado Ativo
                user.Ativo = !user.Ativo;

                // Salvando as alterações
                await _userManager.UpdateAsync(user);
            }
            
            return RedirectToAction(nameof(Index));
        }


        //Apagar o registo de um utilizador(caso não esteja associado a nenhum arrendamento); Não pode apagar nem desativar o seu próprio registo de utilizador.

        /*[HttpPost]

        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create([Bind("Email,Password,Role,Ativo")] FuncionarioAddViewModel funcionario)
        {
            if (!ModelState.IsValid)
            {
                ViewBag.List = new SelectList(await getFuncionarioRoles(), "Id", "Name");
                return View(funcionario);
            }
            var currentuser = await _userManager.GetUserAsync(User);
        }*/
        public async Task<IActionResult> Delete(string userId){


            var gestorId = _userManager.GetUserId(User);

            if (gestorId == userId)
            {

                TempData["ErroMensagem"] = "Você não pode Eliminar a sua própria conta.";

                return RedirectToAction(nameof(Index));

            }

            var user = await _userManager.FindByIdAsync(userId);

            if (user != null){

                if (user.Arrendamentos == null || !user.Arrendamentos.Any())
                    await _userManager.DeleteAsync(user);
                else
                    TempData["ErroMensagem"] = "Este utilizador está associado a arrendamentos e não pode ser excluído.";
                
            }

            return RedirectToAction(nameof(Index));
        }

        private int? GetEmpresaId(){
            var gestorId = _userManager.GetUserId(User);
            var gestor = _userManager.FindByIdAsync(gestorId).Result;

            return gestor.EmpresaId;
        }

    }
}
