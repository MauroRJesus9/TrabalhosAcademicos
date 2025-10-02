using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.EntityFrameworkCore;
using HabitAqui.Data;
using HabitAqui.Models;
using Microsoft.AspNetCore.Authorization;
using static Microsoft.EntityFrameworkCore.DbLoggerCategory;
using MessagePack;
using System.Security.Claims;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Hosting;

namespace HabitAqui.Controllers
{

    [Authorize(Roles = "Admin, Gestor, Funcionario, Cliente")]
    public class ArrendamentosController : Controller
    {
        private readonly ApplicationDbContext _context;

        private readonly UserManager<Utilizadores> _userManager;
        private readonly RoleManager<IdentityRole> _roleManager;

        private readonly IWebHostEnvironment _webHostEnvironment;

        public ArrendamentosController(ApplicationDbContext context, UserManager<Utilizadores>
       userManager, RoleManager<IdentityRole> roleManager, IWebHostEnvironment webHostEnvironment)
        {
            _context = context;
            _userManager = userManager;
            _roleManager = roleManager;
            _webHostEnvironment = webHostEnvironment;
        }

        // GET: Arrendamentos
        public async Task<IActionResult> Index(DateTime? dataInicio, DateTime? dataFim, string? categoria, int? habitacaoId, string? clienteId)
        {

            ViewData["ListaHabitacoes"] = new SelectList(_context.Habitacoes.ToList(), "Id", "Nome");
            ViewData["ListaCategoriaHabitacoes"] = new SelectList(_context.Categorias.ToList(), "Id", "Nome");

            var users = _userManager.Users.ToList();
            var usersOnlyClientes = new List<Utilizadores>();

            foreach(var u in users)
            {
                var isCliente = await _userManager.IsInRoleAsync(u, "Cliente");

                if (isCliente)
                    usersOnlyClientes.Add(u);
                else
                    continue;
            }

            ViewData["ListaClientes"] = new SelectList(usersOnlyClientes, "Id", "NomeCompleto"); 

            var query = _context.Arrendamentos
                                    .Include(a => a.Empresa)
                                    .Include(a => a.Habitacoes)
                                    .Include(a => a.Cliente)
                                    .AsQueryable();

            // Aplicar filtros conforme necessário
            if (dataInicio.HasValue)
                query = query.Where(a => a.DataInicio >= dataInicio.Value);
            if (dataFim.HasValue)
                query = query.Where(a => a.DataFim <= dataFim.Value);
            if (!string.IsNullOrEmpty(categoria))
                query = query.Where(a => string.Equals(a.Habitacoes.Categoria.Nome, categoria, StringComparison.OrdinalIgnoreCase));
            if (habitacaoId != null)
                query = query.Where(a => a.HabitacoesId == habitacaoId.Value);
            if (!string.IsNullOrEmpty(clienteId))
                query = query.Where(a => a.UtilizadoresId.Equals(clienteId));

            var arrendamentos = query.ToList();

            return View(arrendamentos);
        }

        // GET: Arrendamentos/Details/5
        public async Task<IActionResult> Details(int? id)
        {
            if (id == null || _context.Arrendamentos == null)
            {
                return NotFound();
            }

            var arrendamentos = await _context.Arrendamentos
                .Include(a => a.Empresa)
                .Include(a => a.Habitacoes)
                .Include(a => a.Cliente)
                .Include(a => a.EstadoInicial)
                .Include(a => a.EstadoFinal)
                .Include(a => a.EstadoInicial.FuncionarioEntrega)
                .Include(a => a.EstadoFinal.FuncionarioEntrega)
                .FirstOrDefaultAsync(m => m.Id == id);
            if (arrendamentos == null)
            {
                return NotFound();
            }

            return View(arrendamentos);
        }

        private async Task<string> UploadImage(IFormFile imageFile)
        {
            if (imageFile == null || imageFile.Length == 0)
                return new string("Erro, não há fotografia.");


            var uploadsFolder = Path.Combine(_webHostEnvironment.WebRootPath, "img");

            // Garante que o diretório de destino existe, caso contrário, cria-o
            if (!Directory.Exists(uploadsFolder))
            {
                Directory.CreateDirectory(uploadsFolder);
            }

            // Gera um nome de arquivo único para evitar colisões
            var uniqueFileName = Guid.NewGuid().ToString() + "" + imageFile.FileName;

            // Caminho completo para o arquivo no sistema de arquivos
            var filePath = Path.Combine(uploadsFolder, uniqueFileName);

            // Salva a imagem no sistema de arquivos
            using (var fileStream = new FileStream(filePath, FileMode.Create))
            {
                await imageFile.CopyToAsync(fileStream);
            }

            // Retorna a URL da imagem (assumindo que 'img' seja o diretório virtual para as imagens)
            return uniqueFileName;
        }

        public IActionResult ConfirmarArrendamento(int arrendamentoId) //mostrar uma mensagem a dizer arrendamento aceite
        {

            //ir a base de dados e mudar o estado de arrendamentos
            var arrendamento = _context.Arrendamentos.Where(a => a.Id == arrendamentoId).FirstOrDefault();

            if (arrendamento != null)
            {
                arrendamento.Estado = EstadoArrendamento.Confirmado.ToString();
                _context.Arrendamentos.Update(arrendamento);
                _context.SaveChanges();
                return RedirectToAction("Index");
            }  
            else
                return Problem("Erro ao confirmar arrendamento");
        }

        public IActionResult RejeitarArrendamento(int arrendamentoId) //mostrar uma mensagem a dizer arrendamento rejeitado
        {
            //ao rejeitar o arrendamento ele mete rejeitado na base de dadods e depois o user pode remove lo

            var arrendamento = _context.Arrendamentos.Where(a => a.Id == arrendamentoId).FirstOrDefault();

            if (arrendamento != null)
            {
                _context.Arrendamentos.Remove(arrendamento);
                _context.SaveChanges();
                return RedirectToAction("Index");
            }
            else
                return Problem("Erro ao confirmar arrendamento");
        }

        // GET: Arrendamentos/Create
        public IActionResult Create()
        {

            var appUserId = User.FindFirstValue(ClaimTypes.NameIdentifier);

            var utilizador = _userManager.Users.Where(u => u.Id == appUserId).ToList();

            if (User.IsInRole("Cliente"))
            {
                ViewData["ListaCliente"] = new SelectList(utilizador, "Id", "NomeCompleto");
                ViewData["ListaHabitacoes"] = new SelectList(_context.Habitacoes.ToList(), "Id", "Nome");
                //ViewData["ListaEmpresas"] = new SelectList(_context.Empresa..ToList(), "Id", "Nome");
            }
            

            return View();
        }

        // POST: Arrendamentos/Create
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create([Bind("Id,Custo,UtilizadoresId,EmpresaId,HabitacoesId,Estado,EstadoInicial,EstadoFinal,DataInicio,DataFim,ImagemDanos,CaminhoImagem")] Arrendamentos arrendamentos)
        {

            ModelState.Clear(); // Limpar erros existentes

            // Carregar entidades relacionadas do DbContext
            arrendamentos.Cliente = await _userManager.FindByIdAsync(arrendamentos.UtilizadoresId);
            arrendamentos.Empresa = await _context.Empresa.FindAsync(arrendamentos.EmpresaId);
            arrendamentos.Habitacoes = await _context.Habitacoes.FindAsync(arrendamentos.HabitacoesId);
            arrendamentos.Estado = EstadoArrendamento.Pendente.ToString();
            arrendamentos.Custo = Calculate(arrendamentos);

            if (ModelState.IsValid)
            {
                var habitacao = _context.Habitacoes.Where(h => h.Id == arrendamentos.HabitacoesId).FirstOrDefault();

                if (habitacao != null)
                {
                    var habitacaoEmpresaId = habitacao.EmpresaId;

                    var empresa = _context.Empresa.Where(e => e.Id == habitacaoEmpresaId).FirstOrDefault();

                    if (empresa != null)
                    {
                        arrendamentos.Empresa = empresa;
                        _context.Add(arrendamentos);
                        await _context.SaveChangesAsync();
                        return RedirectToAction(nameof(Index));
                    }

                }
            }

            return View(arrendamentos);
        }

        public double Calculate([Bind("Id,Custo,UtilizadoresId,EmpresaId,HabitacoesId,Estado,EstadoInicial,EstadoFinal,DataInicio,DataFim,ImagemDanos,CaminhoImagem")] Arrendamentos arr)
        {

            ViewData["ListaHabitacoes"] = new SelectList(_context.Habitacoes.ToList(), "Id", "Nome");

            double nDias = 0;

            if (arr.DataInicio < DateTime.Now)
            {
                ModelState.AddModelError("DataInicio", "A data de início tem que ser superior a data atual.");
            }
                
            if (arr.DataInicio > arr.DataFim)
            {
                ModelState.AddModelError("DataInicio", "A data de início tem que ser inferior a data de fim.");
            }
                


            var habitacao = _context.Habitacoes
                .Include(m => m.Arrendamentos)
                .Include(m => m.Categoria)
                .FirstOrDefault(v => v.Id == arr.HabitacoesId);
            if (habitacao == null)
            {
                ModelState.AddModelError("HabitacaoId", "Habitação não existe!");
            }

            nDias = (arr.DataFim - arr.DataInicio).TotalDays;
            if (nDias < habitacao.PeriodoMinimoArrendamento)
                ModelState.AddModelError("Periodo Minimo de Arrendamento", "O numero de dias nao atingiu o minimo de dias necessarios para arrendar");
            bool disp = true;

            foreach (Arrendamentos arrendamento in habitacao.Arrendamentos)
            {

                if ((arrendamento.DataInicio <= arr.DataFim && arrendamento.DataFim >= arr.DataInicio) ||
                    (arrendamento.DataFim >= arr.DataInicio && arrendamento.DataInicio <= arr.DataFim))
                {
                    disp = false;
                    break;
                }
            }

            if (!disp)
            {
                ModelState.AddModelError("DataInicio", "Habitação já tem uma reserva neste período de tempo, pedimos desculpa.");
            }

            if (ModelState.IsValid)
            {
                nDias = (arr.DataFim - arr.DataInicio).TotalDays;

                /*Arrendamentos arrendamento = new Arrendamentos();
                arrendamento.DataFim = arr.DataFim;
                arrendamento.DataInicio = arr.DataInicio;
                arrendamento.HabitacoesId = arr.HabitacoesId;*/
                var custo = Math.Round(habitacao.Preco * (int)nDias);
                /*arrendamento.Habitacoes = habitacao;
                arrendamento.Estado = EstadoArrendamento.Pendente.ToString();*/

                return custo;

            }

            return 0;
        }

        // GET: Arrendamentos/Edit/5
        public async Task<IActionResult> Edit(int? id)
        {
            if (id == null || _context.Arrendamentos == null)
            {
                return NotFound();
            }

            var arrendamentos = await _context.Arrendamentos.FindAsync(id);
            if (arrendamentos == null)
            {
                return NotFound();
            }
            ViewData["ListaEmpresas"] = new SelectList(_context.Empresa, "Id", "Nome", arrendamentos.EmpresaId);

            var users = _userManager.Users.ToList();
            var usersOnlyClientes = new List<Utilizadores>();

            foreach (var u in users)
            {
                var isCliente = await _userManager.IsInRoleAsync(u, "Cliente");

                if (isCliente)
                    usersOnlyClientes.Add(u);
                else
                    continue;
            }

            ViewData["ListaClientes"] = new SelectList(usersOnlyClientes, "Id", "NomeCompleto");

            ViewData["ListaHabitacoes"] = new SelectList(_context.Habitacoes.ToList(), "Id", "Nome");
            return View(arrendamentos);
        }

        // POST: Arrendamentos/Edit/5
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(int id, [Bind("Id,Custo,UtilizadoresId,EmpresaId,HabitacoesId,Estado,EstadoInicialId,EstadoFinalId,DataInicio,DataFim")] Arrendamentos arrendamentos)
        {
            if (id != arrendamentos.Id)
            {
                return NotFound();
            }

            ModelState.Clear(); // Limpar erros existentes

            // Carregar entidades relacionadas do DbContext
            arrendamentos.Cliente = await _userManager.FindByIdAsync(arrendamentos.UtilizadoresId);
            arrendamentos.Empresa = await _context.Empresa.FindAsync(arrendamentos.EmpresaId);
            arrendamentos.Habitacoes = await _context.Habitacoes.FindAsync(arrendamentos.HabitacoesId);

            if (ModelState.IsValid)
            {
                try
                {
                    _context.Update(arrendamentos);
                    await _context.SaveChangesAsync();
                }
                catch (DbUpdateConcurrencyException)
                {
                    if (!ArrendamentosExists(arrendamentos.Id))
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

            return View(arrendamentos);
        }

        // GET: Arrendamentos/Delete/5
        public async Task<IActionResult> Delete(int? id)
        {
            if (id == null || _context.Arrendamentos == null)
            {
                return NotFound();
            }

            var arrendamentos = await _context.Arrendamentos
                .Include(a => a.Empresa)
                .FirstOrDefaultAsync(m => m.Id == id);
            if (arrendamentos == null)
            {
                return NotFound();
            }

            return View(arrendamentos);
        }

        // POST: Arrendamentos/Delete/5
        [HttpPost, ActionName("Delete")]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> DeleteConfirmed(int id)
        {
            if (_context.Arrendamentos == null)
            {
                return Problem("Entity set 'ApplicationDbContext.Arrendamentos'  is null.");
            }
            var arrendamentos = await _context.Arrendamentos.FindAsync(id);
            if (arrendamentos != null)
            {
                _context.Arrendamentos.Remove(arrendamentos);
            }
            
            await _context.SaveChangesAsync();
            return RedirectToAction(nameof(Index));
        }

        private bool ArrendamentosExists(int id)
        {
          return (_context.Arrendamentos?.Any(e => e.Id == id)).GetValueOrDefault();
        }

        [Authorize(Roles = "Gestor,Funcionario,Admin")]
        public async Task<IActionResult> EstadoHabitacao(int? id)
        {
            var userManager = HttpContext.RequestServices.GetRequiredService<UserManager<Utilizadores>>();

            var funcionarios = await userManager.GetUsersInRoleAsync("Funcionario");

            ViewData["Funcionarios"] = new SelectList(funcionarios, "Id", "NomeCompleto");

            var arrendamento = await _context.Arrendamentos.FirstOrDefaultAsync(a => a.Id == id);

            if (arrendamento == null)
            {
                return NotFound("Arrendamento nao foi encontrado.\n");
            }

            if (id == null || _context.Arrendamentos == null)
            {
                return NotFound();
            }

            var arrendamentoAux = await _context.Arrendamentos.FindAsync(id);

            if (arrendamentoAux == null)
            {
                return NotFound();
            }

            return View(arrendamentoAux);
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        [Authorize(Roles = "Gestor,Funcionario,Admin")]
        public async Task<IActionResult> EstadoHabitacao(
            int id,
            [Bind("Id,Custo,UtilizadoresId,EmpresaId,HabitacoesId,Estado,EstadoInicialId,EstadoFinalId,DataInicio,DataFim,ImagemDanos,CaminhoImagem")] Arrendamentos arrendamento,
            [Bind("Id,EquipamentosOpcionais,Dano,Observações,FuncionarioEntregaId")] EstadoHabitacao estadoInicial,
            [Bind("Id,EquipamentosOpcionais,Dano,Observações,FuncionarioEntregaId")] EstadoHabitacao estadoFinal,
            [FromForm] List<IFormFile> fotografias
        )
        {

            if (id != arrendamento.Id)
            {
                return NotFound();
            }

            var userManager = HttpContext.RequestServices.GetRequiredService<UserManager<Utilizadores>>();

            var funcionarios = await userManager.GetUsersInRoleAsync("Funcionario");

            ViewData["Funcionarios"] = new SelectList(funcionarios, "Id", "NomeCompleto");

            var arrendamentoComEstado = await _context.Arrendamentos.Include("EstadoInicial").Include("EstadoFinal").FirstOrDefaultAsync(c => c.Id == id);

            if (arrendamentoComEstado == null)
            {
                return NotFound();
            }

            ModelState.Clear(); // Limpar erros existentes

            // Carregar entidades relacionadas do DbContext
            arrendamentoComEstado.Cliente = arrendamento.Cliente;
            arrendamentoComEstado.Empresa = arrendamento.Empresa;
            arrendamentoComEstado.Habitacoes = arrendamento.Habitacoes;

            //arrendamentoComEstado.EstadoInicial = estadoInicial;
            //arrendamentoComEstado.EstadoFinal = estadoFinal;

            // Verificar se o EstadoInicial foi preenchido
            if (!string.IsNullOrWhiteSpace(estadoInicial.EquipamentosOpcionais)
                || !string.IsNullOrWhiteSpace(estadoInicial.Observações))
            {
                arrendamentoComEstado.EstadoInicial = estadoInicial;
            }

            // Verificar se o EstadoFinal foi preenchido
            if (!string.IsNullOrWhiteSpace(estadoFinal.EquipamentosOpcionais)
                || !string.IsNullOrWhiteSpace(estadoFinal.Observações))
            {
                arrendamentoComEstado.EstadoFinal = estadoFinal;
            }

            try
            {

                if (ModelState.IsValid)
                {

                    arrendamentoComEstado.ImagemDanos = fotografias[0];
                    arrendamentoComEstado.CaminhoImagem = await UploadImage(arrendamentoComEstado.ImagemDanos);

                    _context.Update(arrendamentoComEstado);
                    await _context.SaveChangesAsync();
                    return RedirectToAction(nameof(Index));
                }
                else
                {
                    foreach (var modelState in ModelState.Values)
                    {
                        foreach (var error in modelState.Errors)
                        {
                            Console.WriteLine("Error: " + error.ErrorMessage);
                        }
                    }
                }
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!ArrendamentosExists(arrendamento.Id))
                {
                    return NotFound();
                }
                else
                {
                    throw;
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine("Unexpected Error: " + ex.Message);
                throw;
            }

            ViewData["ListaHabitacoes"] = new SelectList(_context.Habitacoes.ToList(), "Id", "Nome", arrendamentoComEstado.Id);

            return View(arrendamentoComEstado);
        }

    }
}
