using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.EntityFrameworkCore;
using HabitAqui.Data;
using HabitAqui.Models;
using HabitAqui.ViewModels;
using Microsoft.AspNetCore.Authorization;
using System.ComponentModel.DataAnnotations;
using Microsoft.AspNetCore.Identity;

namespace HabitAqui.Controllers
{

    [Authorize(Roles = "Admin")]
    public class EmpresasController : Controller
    {
        private readonly ApplicationDbContext _context;

        private readonly UserManager<Utilizadores> _userManager;

        public EmpresasController(ApplicationDbContext context, UserManager<Utilizadores>
       userManager)
        {
            _context = context;
            _userManager = userManager;
        }

        // GET: Empresas
        
        public async Task<IActionResult> Index(bool? ativo)
        {
            /*return _context.Empresa != null ? 
                        View(await _context.Empresa.ToListAsync()) :
                        Problem("Entity set 'ApplicationDbContext.Empresa'  is null.");*/
            ViewData["ListaDeEmpresas"] = new SelectList(_context.Empresa.OrderBy(e => e.Nome).ToList(), "Id", "Nome");

            if (ativo != null)
            {
                if (ativo == true)
                    ViewData["Title"] = "Lista de Empresas Activas";
                else
                    ViewData["Title"] = "Lista de Empresas Inactivas";

                return View(await _context.Empresa.
                    Where(e => e.Ativo == ativo).OrderBy(e => e.Nome).ToListAsync()
                    );
            }
            else
            {
                ViewData["Title"] = "Lista de Empresas";
                return View(await _context.Empresa.OrderBy(e => e.Nome).ToListAsync());
            }

        }

        [HttpPost]
        //[Authorize(Roles = "Admin")]
        public IActionResult Index(string TextoAPesquisar, int empresaId)
        {
            ViewData["ListaDeEmpresas"] = new SelectList(_context.Empresa.OrderBy(e => e.Nome).ToList(), "Id", "Nome");

            if (!string.IsNullOrWhiteSpace(TextoAPesquisar))
            {
                var resultado = from e in _context.Empresa
                                where e.Nome.Contains(TextoAPesquisar) && e.Id == empresaId
                                select e;
                return View(resultado);
            }
            else
            {
                ViewData["Title"] = "Lista de Empresas";
                var empresas = _context.Empresa.OrderBy(e => e.Nome).ToListAsync();

                return View(empresas);
            }
        }

        //[Authorize(Roles = "Admin")]
        public async Task<IActionResult> Search(string? TextoAPesquisar)
        {
            PesquisaEmpresaViewModel pesquisaVM = new PesquisaEmpresaViewModel();
            ViewData["Title"] = "Pesquisar empresas";

            if (string.IsNullOrWhiteSpace(TextoAPesquisar))
                pesquisaVM.ListaDeEmpresas = await _context.Empresa.OrderBy(c => c.Nome).ToListAsync();
            else
            {
                /*pesquisaEmpresa.ListaDeEmpresas =
                    await _context.Empresa.Where(e => e.Nome.Contains(pesquisaEmpresa.TextoAPesquisar)
                                                || e.Utilizadores?.Contains(pesquisaEmpresa.TextoAPesquisar)
                                                || c.DescricaoResumida.Contains(pesquisaEmpresa.TextoAPesquisar)
                                                ).OrderBy(c => c.Nome).ToListAsync();*/

                pesquisaVM.ListaDeEmpresas = await _context.Empresa
                                                            .Where(e => e.Nome.Contains(TextoAPesquisar))
                                                            .OrderBy(e => e.Nome)
                                                            .ToListAsync();
                pesquisaVM.TextoAPesquisar = TextoAPesquisar;
                foreach (Empresa e in pesquisaVM.ListaDeEmpresas)
                {
                    e.Nome = AltCorSubSTR(e.Nome, pesquisaVM.TextoAPesquisar);
                }
            }
            pesquisaVM.NumResultados = pesquisaVM.ListaDeEmpresas.Count();

            return View(pesquisaVM);
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        //[Authorize(Roles = "Admin")]
        public async Task<IActionResult> Search(
                    [Bind("TextoAPesquisar")] PesquisaEmpresaViewModel pesquisaEmpresa
                    )
        {
            ViewData["Title"] = "Pesquisar empresa";
            if (string.IsNullOrEmpty(pesquisaEmpresa.TextoAPesquisar))
            {
                pesquisaEmpresa.ListaDeEmpresas = await _context.Empresa.OrderBy(e => e.Nome).ToListAsync();
                pesquisaEmpresa.NumResultados = pesquisaEmpresa.ListaDeEmpresas.Count();
            }
            else
            {
                /*pesquisaEmpresa.ListaDeEmpresas =
                    await _context.Empresa.Where(e => e.Nome.Contains(pesquisaEmpresa.TextoAPesquisar)
                                                || e.Utilizadores?.Contains(pesquisaEmpresa.TextoAPesquisar)
                                                || c.DescricaoResumida.Contains(pesquisaEmpresa.TextoAPesquisar)
                                                ).OrderBy(c => c.Nome).ToListAsync();*/
                
                pesquisaEmpresa.ListaDeEmpresas = await _context.Empresa
                                                            .Where(e => e.Nome.Contains(pesquisaEmpresa.TextoAPesquisar))
                                                            .OrderBy(e => e.Nome)
                                                            .ToListAsync();
                pesquisaEmpresa.NumResultados = pesquisaEmpresa.ListaDeEmpresas.Count();

                foreach (Empresa e in pesquisaEmpresa.ListaDeEmpresas)
                {
                    e.Nome = AltCorSubSTR(e.Nome, pesquisaEmpresa.TextoAPesquisar);
                }
            }
            return View(pesquisaEmpresa);
        }

        // GET: Empresas/Details/5
        //[Authorize(Roles = "Admin")]
        public async Task<IActionResult> Details(int? id)
        {
            if (id == null || _context.Empresa == null)
            {
                return NotFound();
            }

            var empresa = await _context.Empresa
                .FirstOrDefaultAsync(m => m.Id == id);
            if (empresa == null)
            {
                return NotFound();
            }

            var utilizadoresEmpresa = await _context.Utilizadores.Where(u => u.EmpresaId == empresa.Id).ToListAsync();

            if (empresa.Utilizadores != null)
            {
                ViewData["FuncionariosLista"] = new SelectList(utilizadoresEmpresa, "Id", "NomeCompleto");
            }

            return View(empresa);
        }

        // GET: Empresas/Create
        //[Authorize(Roles = "Admin")]
        public IActionResult Create()
        {
            return View();
        }

        // POST: Empresas/Create
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> Create([Bind("Id,Nome,Avaliacao,Ativo")] Empresa empresa)
        {
            if (ModelState.IsValid)
            {
                //Aqui quando cria a empresa tem que criar um perfil para o gestor dessa empresa

                _context.Add(empresa);
                await _context.SaveChangesAsync();

                var empresaManager = new Utilizadores
                {
                    UserName = "gestor" + empresa.Nome + empresa.Id + "@gmail.com",
                    Email = "gestor" + empresa.Nome + empresa.Id + "@gmail.com",
                    NomeCompleto = "gestor" + empresa.Nome + empresa.Id,
                    EmailConfirmed = true,
                    PhoneNumberConfirmed = true
                };

                empresaManager.Empresa = empresa;
                empresaManager.EmpresaId = empresa.Id;

                var user = await _userManager.FindByEmailAsync(empresaManager.Email);
                if (user == null)
                {
                    await _userManager.CreateAsync(empresaManager, "Gestor" + empresa.Nome + empresa.Id + "@");
                    await _userManager.AddToRoleAsync(empresaManager,
                    Roles.Gestor.ToString());
                }

                return RedirectToAction(nameof(Index));
            }
            return View(empresa);
        }

        // GET: Empresas/Edit/5
        //[Authorize(Roles = "Admin")]
        public async Task<IActionResult> Edit(int? id)
        {
            if (id == null || _context.Empresa == null)
            {
                return NotFound();
            }

            var empresa = await _context.Empresa.FindAsync(id);
            if (empresa == null)
            {
                return NotFound();
            }

            return View(empresa);
        }

        // POST: Empresas/Edit/5
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        //[Authorize(Roles = "Admin")]
        public async Task<IActionResult> Edit(int id, [Bind("Id,Nome,Avaliacao,Ativo")] Empresa empresa/*, string utilizadorSelecionado*/)
        {
            if (id != empresa.Id)
            {
                return NotFound();
            }

            if (ModelState.IsValid)
            {
                try
                {
                    _context.Update(empresa);
                    await _context.SaveChangesAsync();
                }
                catch (DbUpdateConcurrencyException)
                {
                    if (!EmpresaExists(empresa.Id))
                    {
                        return NotFound();
                    }
                    else
                    {
                        throw;
                    }
                }
                return RedirectToAction(nameof(Index));
            }

            return View(empresa);
        }
        
        // GET: Empresas/Delete/5
       // [Authorize(Roles = "Admin")]
        public async Task<IActionResult> Delete(int? id)
        {
            if (id == null || _context.Empresa == null)
            {
                return NotFound();
            }

            var empresa = await _context.Empresa
                .FirstOrDefaultAsync(m => m.Id == id);

            if (empresa == null)
            {
                return NotFound();
            }

            return View(empresa);
        }

        // POST: Empresas/Delete/5
        [HttpPost, ActionName("Delete")]
        [ValidateAntiForgeryToken]
        //[Authorize(Roles = "Admin")]
        public async Task<IActionResult> DeleteConfirmed(int id) // TODO este delete so pode funcionar se nao existirem habitacoes atribuidas a esta empresa
        {
            if (_context.Empresa == null)
            {
                return Problem("Entity set 'ApplicationDbContext.Empresa'  is null.");
            }

            var empresa = await _context.Empresa.FindAsync(id);
            if (empresa != null)
            {

                //verificar se a empresa nao tem nenhuma habitacao, se tiver da return porque nao pode eliminar
                if (empresa.Habitacoes?.Count() > 0)
                {
					return BadRequest("Empresa tem Habitacoes atribuidas");
				}

                //remover o perfil do gestor da empresa quando a empresa é apagada
                var gestores = await _userManager.GetUsersInRoleAsync("Gestor");
                var gestoresEmpresa = new List<Utilizadores>();
                if (gestores != null)
                {

                    foreach(var g in gestores)
                    {
                        
                        if(g.EmpresaId == empresa.Id)
                        {
                            gestoresEmpresa.Add(g);
                        }

                    }
                }

                foreach(var gE in gestoresEmpresa)
                {
                    await _userManager.DeleteAsync(gE);
                }

                //remover funcionarios da empresa
                var funcionariosEmpresa = await _userManager.Users
                .Where(u => u.EmpresaId == empresa.Id).ToListAsync();

                if(funcionariosEmpresa != null)
                {
                    foreach (var user in funcionariosEmpresa)
                    {
                        await _userManager.DeleteAsync(user);
                    }
                    
                }

                var arrendamentosEmpresa = await _context.Arrendamentos
                    .Where(a => a.EmpresaId == empresa.Id).ToArrayAsync();

                if (arrendamentosEmpresa != null)
                {
                    foreach (var arrendamento in arrendamentosEmpresa)
                    {
                        _context.Arrendamentos.Remove(arrendamento);
                    }

                }

                _context.Empresa.Remove(empresa);
            }
            
            await _context.SaveChangesAsync();
            return RedirectToAction(nameof(Index));
        }

        private bool EmpresaExists(int id)
        {
          return (_context.Empresa?.Any(e => e.Id == id)).GetValueOrDefault();
        }

        public string AltCorSubSTR(string txtOriginal, string txtPesquisa)
        {
            string txtAlterado = string.Empty;

            if (!string.IsNullOrEmpty(txtOriginal))
            {
                string[] split = txtOriginal.Split(" ");

                foreach (string str in split)
                {
                    if (str.ToLower().Contains(txtPesquisa.ToLower()))
                    {
                        string a = string.Empty;
                        int b = 0;

                        for (int i = 0; i < str.Length; i++)
                        {
                            if (str.ToLower().Substring(i, txtPesquisa.Length) == txtPesquisa.ToLower())
                            {
                                a = str.Substring(i, txtPesquisa.Length);
                                b = i;
                                break;
                            }
                        }

                        txtAlterado += str + " ";

                        txtAlterado = txtAlterado.Replace(str.Substring(b, txtPesquisa.Length),
                            "<span class=\"bg-warning\">" + a + "</span>");
                    }
                    else
                        txtAlterado += str + " ";
                }
            }
            else
                txtAlterado = txtOriginal;

            return txtAlterado;
        }
    }
}
