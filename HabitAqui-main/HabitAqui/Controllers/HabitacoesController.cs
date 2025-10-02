using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.EntityFrameworkCore;
using HabitAqui.Data;
using HabitAqui.Models;

namespace HabitAqui.Controllers
{
    public class HabitacoesController : Controller
    {
        private readonly ApplicationDbContext _context;

        public HabitacoesController(ApplicationDbContext context)
        {
            _context = context;
        }

        // GET: Habitacoes
        public async Task<IActionResult> Index()
        {
            var applicationDbContext = _context.Habitacoes.Include(h => h.Categoria).Include(h => h.Empresa);
            return View(await applicationDbContext.ToListAsync());
        }

        // GET: Habitacoes/Details/5
        public async Task<IActionResult> Details(int? id)
        {
            if (id == null || _context.Habitacoes == null)
            {
                return NotFound();
            }

            var habitacoes = await _context.Habitacoes
                .Include(h => h.Categoria)
                .Include(h => h.Empresa)
                .FirstOrDefaultAsync(m => m.Id == id);
            if (habitacoes == null)
            {
                return NotFound();
            }

            return View(habitacoes);
        }

        // GET: Habitacoes/Create
        public IActionResult Create()
        {
            ViewData["CategoriaId"] = new SelectList(_context.Categorias, "Id", "Nome");
            ViewData["EmpresaId"] = new SelectList(_context.Empresa, "Id", "Nome");
            return View();
        }

        // POST: Habitacoes/Create
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Create([Bind("Id,Nome,Descricao,DataInicio,DataFim,Localidade,Preco,CategoriaId,PeriodoMinimoArrendamento,Estado,Ativo,EmpresaId,FotografiaCaminho")] Habitacoes habitacoes)
        {
            if (ModelState.IsValid)
            {
                _context.Add(habitacoes);
                await _context.SaveChangesAsync();
                return RedirectToAction(nameof(Index));
            }
            ViewData["CategoriaId"] = new SelectList(_context.Categorias, "Id", "Nome", habitacoes.CategoriaId);
            ViewData["EmpresaId"] = new SelectList(_context.Empresa, "Id", "Nome", habitacoes.EmpresaId);
            return View(habitacoes);
        }

        // GET: Habitacoes/Edit/5
        public async Task<IActionResult> Edit(int? id)
        {
            if (id == null || _context.Habitacoes == null)
            {
                return NotFound();
            }

            var habitacoes = await _context.Habitacoes.FindAsync(id);
            if (habitacoes == null)
            {
                return NotFound();
            }
            ViewData["CategoriaId"] = new SelectList(_context.Categorias, "Id", "Nome", habitacoes.CategoriaId);
            ViewData["EmpresaId"] = new SelectList(_context.Empresa, "Id", "Nome", habitacoes.EmpresaId);
            return View(habitacoes);
        }

        // POST: Habitacoes/Edit/5
        // To protect from overposting attacks, enable the specific properties you want to bind to.
        // For more details, see http://go.microsoft.com/fwlink/?LinkId=317598.
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> Edit(int id, [Bind("Id,Nome,Descricao,DataInicio,DataFim,Localidade,Preco,CategoriaId,PeriodoMinimoArrendamento,Estado,Ativo,EmpresaId,FotografiaCaminho")] Habitacoes habitacoes)
        {
            if (id != habitacoes.Id)
            {
                return NotFound();
            }

            if (ModelState.IsValid)
            {
                try
                {
                    _context.Update(habitacoes);
                    await _context.SaveChangesAsync();
                }
                catch (DbUpdateConcurrencyException)
                {
                    if (!HabitacoesExists(habitacoes.Id))
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
            ViewData["CategoriaId"] = new SelectList(_context.Categorias, "Id", "Nome", habitacoes.CategoriaId);
            ViewData["EmpresaId"] = new SelectList(_context.Empresa, "Id", "Nome", habitacoes.EmpresaId);
            return View(habitacoes);
        }

        // GET: Habitacoes/Delete/5
        public async Task<IActionResult> Delete(int? id)
        {
            if (id == null || _context.Habitacoes == null)
            {
                return NotFound();
            }

            var habitacoes = await _context.Habitacoes
                .Include(h => h.Categoria)
                .Include(h => h.Empresa)
                .FirstOrDefaultAsync(m => m.Id == id);
            if (habitacoes == null)
            {
                return NotFound();
            }

            return View(habitacoes);
        }

        // POST: Habitacoes/Delete/5
        [HttpPost, ActionName("Delete")]
        [ValidateAntiForgeryToken]
        public async Task<IActionResult> DeleteConfirmed(int id)
        {
            if (_context.Habitacoes == null)
            {
                return Problem("Entity set 'ApplicationDbContext.Habitacoes'  is null.");
            }
            var habitacoes = await _context.Habitacoes.FindAsync(id);
            if (habitacoes != null)
            {
                _context.Habitacoes.Remove(habitacoes);
            }
            
            await _context.SaveChangesAsync();
            return RedirectToAction(nameof(Index));
        }

        private bool HabitacoesExists(int id)
        {
          return (_context.Habitacoes?.Any(e => e.Id == id)).GetValueOrDefault();
        }
    }
}
